angular.module('app.services', [])

.factory('radioDataService', function ($http) {
    return {
        all: function (forceRefresh) {
            return $http.get("https://raw.githubusercontent.com/vmanikandan001/Vaanoli/master/list.json");
        }
    };
})
.factory('userDataService', function ($q) {
    var fileName = "userRadios.json";

    return {
        readUserRadios: readUserRadios,
        writeUserRadios: writeUserRadios
    };


    function readUserRadios() {
        var pathToFile = cordova.file.dataDirectory + "/" + fileName;

        var deferred = $q.defer();

        window.resolveLocalFileSystemURL(pathToFile, function (fileEntry) {
            fileEntry.file(function (file) {
                var reader = new FileReader();

                reader.onloadend = function (e) {
                    deferred.resolve(JSON.parse(e.target.result));
                };

                reader.readAsText(file);
            }, fileErrorHandler.bind(null, fileName));
        }, fileErrorHandler.bind(null, fileName));

        return deferred.promise;
    }

    function writeUserRadios(data) {

        data = JSON.stringify(data, null, '\t');
        window.resolveLocalFileSystemURL(cordova.file.dataDirectory, function (directoryEntry) {
            directoryEntry.getFile(fileName, { create: true }, function (fileEntry) {
                fileEntry.createWriter(function (fileWriter) {

                    fileWriter.onerror = function (e) {
                        // todo: show error message
                        console.log('Write failed: ' + e.toString());
                    };

                    var blob = new Blob([data], { type: 'text/plain' });
                    fileWriter.write(blob);
                }, fileErrorHandler.bind(null, fileName));
            }, fileErrorHandler.bind(null, fileName));
        }, fileErrorHandler.bind(null, fileName));
    }

    function fileErrorHandler (fileName, e) {
        var msg = '';

        switch (e.code) {
            case FileError.QUOTA_EXCEEDED_ERR:
                msg = 'Storage quota exceeded';
                break;
            case FileError.NOT_FOUND_ERR:
                msg = 'File not found';
                break;
            case FileError.SECURITY_ERR:
                msg = 'Security error';
                break;
            case FileError.INVALID_MODIFICATION_ERR:
                msg = 'Invalid modification';
                break;
            case FileError.INVALID_STATE_ERR:
                msg = 'Invalid state';
                break;
            default:
                msg = 'Unknown error';
                break;
        };

        // todo: show error
        console.log('Error (' + fileName + '): ' + msg);
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
