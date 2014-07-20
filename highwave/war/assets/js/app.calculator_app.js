App.CalculatorApp = function() {
    var CalculatorApp = {};

    var Deposit = Backbone.Model.extend();

    var Deposits = Backbone.Collection.extend({
        model: Deposit,

        showMessage: function(message) {
            console.log(message);
        },

        initialize: function() {
            var self = this;

            _.bindAll(this, "search");
            App.vent.on("search:searchQuery", function(query) {
                self.search(query);
            });

            this.loading = false;
        },

        search: function(searchQuery) {
            var self = this;

            self.reset([]);

            this.fetchDeposits(searchQuery, function(deposits) {
                if (deposits.length > 0) {
                    self.reset(deposits);
                }
                else {
                    App.vent.trigger("search:noResults");
                }
            });
        },

        fetchDeposits: function(searchQuery, callback) {
            if (this.loading) {
                return;
            }

            this.loading = true;

            var self = this;
            App.vent.trigger("search:start");

            $.ajax({
                url: "/_ah/api/deposits/v0/deposits",
                data: searchQuery,
                success: function(res) {
                    App.vent.trigger("search:stop");
                    if (res.items.length == 0) {
                        callback([]);
                        return [];
                    }
                    if (res.items) {
                        self.page++;
                        self.totalItems = res.totalItems;
                        var searchResults = [];
                        _.each(res.items, function(item) {
                            searchResults[searchResults.length] = new Deposit(item);
                        });
                        callback(searchResults);
                        self.loading = false;
                        return searchResults;
                    }
                    else if (res.error) {
                        App.vent.trigger("search:error");
                        self.loading = false;
                    }
                    return null;
                }
            });
        }
    });

    var SearchModel = Backbone.Model.extend({
        defaults: {
            amount: 1000000,
            period: 35
        }
    });

    CalculatorApp.Deposits = new Deposits();
    CalculatorApp.SearchModel = new SearchModel();

    return CalculatorApp;
}();

App.addInitializer(function() {
    App.CalculatorApp.DepositList.showDeposits(App.CalculatorApp.Deposits);
    App.CalculatorApp.Search.showSearch(App.CalculatorApp.SearchModel);
});