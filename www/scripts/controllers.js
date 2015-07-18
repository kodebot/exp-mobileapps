angular.module('app.controllers', [])
.controller('radioController', function ($scope, $ionicLoading, $ionicPopup, $timeout, radioDataService) {
    var media;
    var vm = this;
    vm.isStopped = false;
    vm.currentRadio = null;
    vm.volume = 50;
    var mediaStartingPromise = null;

    activate();

    $scope.$watch('vm.volume', function () {
        if (media) {
            media.setVolume(vm.volume / 100); // this works in android
        }
    });

    vm.play = function (radio) {
        vm.currentRadio = radio;
        vm.isStopped = false;

        if (media) {
            media.release();
        }

        media = new Media(radio.streamUrl, null, mediaError, mediaStatus);

        media.play();
    }

    function mediaError(error) {
        showAlert("Error", "Unable to play this radio. Please try again later.");

        //MediaError.MEDIA_ERR_ABORTED = 1
        //MediaError.MEDIA_ERR_NETWORK = 2
        //MediaError.MEDIA_ERR_DECODE = 3
        //MediaError.MEDIA_ERR_NONE_SUPPORTED = 4
    }

    function mediaStatus(status) {
        if (status === Media.MEDIA_STARTING) {
            showLoading("Starting " + vm.currentRadio.name + "...");
            mediaStartingPromise = $timeout(onStrukStarting, 15000); // wait 15 seconds
        }

        if (status === Media.MEDIA_RUNNING) {
            $timeout.cancel(mediaStartingPromise);
            hideLoading(); // hide the status started when media is starting
        }

        function onStrukStarting() {
            hideLoading();
            showAlert("Error", "Unable to play this radio. Please try again later.");
        }
        //Media.MEDIA_NONE = 0;
        //Media.MEDIA_STARTING = 1;
        //Media.MEDIA_RUNNING = 2;
        //Media.MEDIA_PAUSED = 3;
        //Media.MEDIA_STOPPED = 4;
    }

    vm.stopToggle = function () {

        // when no radio is selected and play is pressed then select the first one in the list and play
        if (!vm.currentRadio) {
            vm.play(vm.radios[0]);
            return;
        }

        showLoading();
        if (media) {
            if (vm.isStopped) {
                vm.isStopped = false;
                media.play();

            } else {
                vm.isStopped = true;
                media.stop();
            }
        }

        hideLoading();
    }

    vm.updateRadio = function () {
        vm.radios = getRadios(true);
        $scope.$broadcast('scroll.refreshComplete');
    }

    function activate() {
        showLoading();
        document.addEventListener("deviceready", onDeviceReady, false);
    }

    function showLoading(msg) {
        $ionicLoading.show({
            template: msg || 'Please wait...'
        });
    }

    function hideLoading() {
        $ionicLoading.hide();
    }

    function onDeviceReady() {
        hideLoading();
        getRadios();
    }

    function getRadios() {
        showLoading();
        radioDataService.all()
        .then(function (response) {
            vm.radios = response.data.radios;
        })
        .catch(function () {
            showInternetIssueAlert("Error", "Unable to retrieve radio list. Please make sure you have internet access. If the problem persist, try again later.", getRadios);
        })
        .finally(function () {
            hideLoading();
        });
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
})
.controller('aboutController', function ($scope) { });

