angular.module('app.controllers', [])
.controller('radioController', function ($scope, $ionicLoading, radioDataService) {
    var media;
    var vm = this;
    vm.isPaused = false;
    vm.currentRadio = null;
    vm.volume = 50;



    activate();

    $scope.$watch('vm.volume', function () {
        if (media) {
            media.setVolume(vm.volume / 100); // this works in android
        }
    });

    vm.play = function (radio) {
        showLoading();
        vm.currentRadio = radio;
        vm.isPaused = false;

        if (media) {
            media.release();
        }

        media = new Media(radio.streamUrl);
        media.play();
        hideLoading();
    }

    vm.pauseToggle = function () {

        // when no radio is selected and play is pressed then select the first one in the list and play
        if (!vm.currentRadio) {
            vm.play(vm.radios[0]);
            return;
        }

        showLoading();
        if (media) {
            if (vm.isPaused) {
                vm.isPaused = false;
                media.play();

            } else {
                vm.isPaused = true;
                media.pause();
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

    function showLoading() {
        $ionicLoading.show({
            template: 'Please wait...'
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
        .finally(function () {
            hideLoading();
        });
    }
})
.controller('aboutController', function ($scope) { });

