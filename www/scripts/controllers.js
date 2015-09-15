angular.module('app.controllers', [])
.controller('radioController', function ($scope, $ionicPlatform, $timeout, alertService, radioDataService, userDataService) {
    var mediaStartingPromise;
    var vm = this;

    // #region View Model
    vm.isStopped = false;
    vm.currentRadio = null;
    vm.volume = 50;
    vm.play = play;
    vm.stopToggle = stopToggle;
    vm.updateRadio = updateRadio;
    vm.toggleFav = toggleNav;
    vm.toggleNavFav = toggleNavFav;
    vm.gotoPrev = gotoPrev;
    vm.gotoNext = gotoNext;
    // #endregion

    activate();

    // #region private functions
    function activate() {
        alertService.showLoading();
        $ionicPlatform.ready(onDeviceReady);

    }

    function play(radio) {
        alertService.showLoading("Starting " + radio.name + "...");
        vm.currentRadio = getAndSyncCurrentRadioById(radio.id);
        vm.isStopped = false;
        mediaStartingPromise = $timeout(onStrukStarting, 15000); // wait 15 seconds
        BackgroundAudioPlayer.play(function () {
            $timeout.cancel(mediaStartingPromise);
            alertService.hideLoading();
        }, onPlayError, radio.streamUrl, radio.id);
    }

    function stopToggle() {
        // when no radio is selected and play is pressed then select the first one in the list and play
        if (!vm.currentRadio) {
            vm.play(vm.radios[0]);
            return;
        }

        if (vm.isStopped) {
            vm.play(vm.currentRadio);

        } else {
            vm.isStopped = true;
            BackgroundAudioPlayer.stop(null, null);
        }
    }

    function updateRadio() {
        vm.radios = getRadios(true);
        $scope.$broadcast('scroll.refreshComplete');
    }

    function toggleNav(radio) {
        radio.fav = !radio.fav;
        userDataService.writeUserRadios(vm.radios);
    }

    function gotoPrev() {
        play(vm.radios.prev());
    }

    function gotoNext() {
        play(vm.radios.next());
    }

    function toggleNavFav() {

    }

    function onStrukStarting() {
        alertService.hideLoading();
        alertService.showAlert("Error", "Unable to play this radio. Please try again later.");
    }

    function hookVolumeControl() {
        $scope.$watch('vm.volume', function () {
            if (BackgroundAudioPlayer) {
                BackgroundAudioPlayer.setVolume(null, null, vm.volume / 100); // this works in android
            }
        });
    }

    function onPlayError() {
        $timeout.cancel(mediaStartingPromise);
        alertService.hideLoading();
        alertService.showAlert("Error", "Unable to play this radio. Please try again later.");

    }

    function onDeviceReady() {
        setupScreen();
    }

    function setupScreen() {
        alertService.hideLoading();
        getRadios()
        .then(function () {
            hookVolumeControl();
            initialisePlayer();
        });

    }

    function initialisePlayer() {
        BackgroundAudioPlayer.getStatus(function (status) {
            vm.isStopped = !(status == 1);
        }, function () { a });

        BackgroundAudioPlayer.getVolume(function (volume) {
            vm.volume = Number.parseFloat(volume) * 100;
        }, function () { });

        BackgroundAudioPlayer.getCurrentRadio(function (radioId) {
            if (radioId) {
                vm.currentRadio = getAndSyncCurrentRadioById(radioId);
            }
        }, null);
    }

    function getRadios() {
        alertService.showLoading();
        return radioDataService.all()
        .then(function (response) {
            vm.radios = response.data.radios;
            userDataService.readUserRadios()
            .then(function (result) {
                if (result) { // file exist - update
                    var favs = result.filter(function (item) {
                        if (item.fav) return true;
                    });

                    favs.forEach(function (item) {
                        vm.radios.forEach(function (radio) {
                            if (radio.id === item.id) radio.fav = true;
                        });
                    });
                }
                userDataService.writeUserRadios(vm.radios);
            });

        })
        .catch(function (e) {
            alertService.showInternetIssueAlert("Error", "Unable to retrieve radio list. Please make sure you have internet access. If the problem persist, try again later.", getRadios);
        })
        .finally(function () {
            alertService.hideLoading();
        });
    }

    function getAndSyncCurrentRadioById(radioId) {
        return vm.radios.filter(function (radio, index) {
            if (radio.id == radioId) {
                vm.radios.current = index;
                return true;
            }
        })[0];
    }
    Array.prototype.next = function () {
        this.current++;
        if (this.current === this.length) this.current = 0;
        return this[this.current];
    };
    Array.prototype.prev = function () {
        this.current--;
        if (this.current === -1) this.current = (this.length - 1);
        return this[this.current];
    };
    Array.prototype.current = 0;
    // #endregion

})
.controller('aboutController', function ($scope) { });

