angular.module('app.services', [])

.factory('radioDataService', function ($http) {
    return {
        all: function (forceRefresh) {
            return $http.get("https://raw.githubusercontent.com/vmanikandan001/Vaanoli/master/list.json");
        }
    };
});
