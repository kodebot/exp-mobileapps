angular.module('app.services', [])

.factory('radioDataService', function ($http) {
    return {
        all: function (forceRefresh) {
            return $http.get("https://raw.githubusercontent.com/vmanikandan001/Vaanoli/master/list.json");
        }
    };
})
.factory('userDataService', function () {
    
    var KEY_FAV_RADIOS = 'com.qubits.vaanoli.fav.radios';
    var KEY_FAV_PREF = 'com.qubits.vaanoli.fav.pref';
    return {
        setFavRadios: setFavRadios,
        getFavRadios: getFavRadios,
        setFavPref: setFavPref,
        getFavPref:getFavPref
    };


    function setFavRadios(commaSepRadioIds) {
        window.localStorage.setItem(KEY_FAV_RADIOS, commaSepRadioIds);
    }

    function getFavRadios() {
        return window.localStorage.getItem(KEY_FAV_RADIOS);
    }

    function setFavPref(favPref) {
        window.localStorage.setItem(KEY_FAV_PREF, favPref);
    }

    function getFavPref() {
        return window.localStorage.getItem(KEY_FAV_PREF);
    }
})
.factory('alertService', function ($ionicLoading, $ionicPopup) {


    return {
        showLoading: showLoading,
        hideLoading: hideLoading,
        showAlert: showAlert,
        showInternetIssueAlert: showInternetIssueAlert
    };

    function showLoading(msg) {
        $ionicLoading.show({
            template: msg || 'Please wait...'
        });
    }

    function hideLoading() {
        $ionicLoading.hide();
    }

    function showAlert(title, message) {
        var alertPopup = $ionicPopup.alert({
            title: title,
            template: message
        });
    }

    function showInternetIssueAlert(title, message, onRetry) {
        $ionicPopup.show({
            template: message,
            title: title,
            buttons: [
              {
                  text: "Re-try",
                  type: "button-assertive",
                  onTap: function (e) {
                      onRetry();
                  }
              },
              {
                  text: "Close",
                  type: "button-positive"
              }
            ]
        });
    }

});
