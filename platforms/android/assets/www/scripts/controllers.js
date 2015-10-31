angular.module('app.controllers', [])
.controller('radioController', function ($scope, $ionicPlatform, $ionicPopup, $ionicScrollDelegate, $timeout, $interval, alertService, radioDataService, userDataService) {
    var mediaStartingPromise;
    var mediaBufferingPromise;
    var vm = this;

    // #region View Model
    vm.isStopped = false;
    vm.currentRadio = null;
    vm.currentStatus = null;
    vm.showOnlyFav = false;
    vm.timerSettings = { duration: 0 };
    vm.volume = 50;
    vm.play = play;
    vm.stopToggle = stopToggle;
    vm.updateRadio = updateRadio;
    vm.toggleFav = toggleFav;
    vm.toggleNavFav = toggleNavFav;
    vm.gotoPrev = gotoPrev;
    vm.gotoNext = gotoNext;
    vm.showTimerPopup = showTimerPopup;
    vm.scrollToTop = scrollToTop;
    vm.getRandomThumpIconColor = getRandomThumpIconColor;
    // #endregion

    activate();

    // #region private functions
    function activate() {
        augmentArrayPrototype();
        alertService.showLoading();
        $ionicPlatform.ready(onDeviceReady);
    }

    function play(radio) {
        updateStatus("Starting " + radio.name + "...");
        vm.currentRadio = getAndSyncCurrentRadioById(radio.id);
        vm.isStopped = false;
        mediaStartingPromise = $timeout(onStrukStarting, 15000); // wait 15 seconds
        BackgroundAudioPlayer.play(function () {
            $timeout.cancel(mediaStartingPromise);
            waitOnBuffering();
        }, onPlayError, radio.streamUrl, radio.id, radio.name);
    }


    function stopToggle() {
        // when no radio is selected and play is pressed then select the first one in the list and play
        if (!vm.currentRadio) {
            var radiosInContext = getFavContextRadios(); // get fav radios only when show fav only is on
            if (radiosInContext.length) {
                vm.play(radiosInContext[0]);
            } else {
                vm.play(vm.radios[0]); // play the first from all list when none is available in filtered list
            }
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

    function toggleFav(radio) {
        radio.fav = !radio.fav;

        var favRadios = vm.radios.filter(function (item) { return item.fav; })
            .map(function (item) { return item.id; }).valueOf();
        userDataService.setFavRadios(favRadios);
    }

    function gotoPrev() {
        play(getFavContextRadios().prev());
    }

    function gotoNext() {
        play(getFavContextRadios().next());
    }

    function toggleNavFav() {
        vm.showOnlyFav = !vm.showOnlyFav;
        userDataService.setFavPref(vm.showOnlyFav || 0);
        scrollToTop();
    }

    function getFavContextRadios() {
        var radios = vm.radios;

        if (vm.showOnlyFav) {
            radios = vm.radios.filter(function (radio) { return radio.fav; });

            if(radios.length === 0){
                vm.showOnlyFav = false;
                return vm.radios;
            }
        }

        if (vm.currentRadio) {
            updateCurrentRadioById(vm.currentRadio.id);
        }
        return radios;

        function updateCurrentRadioById(radioId) {
            radios.forEach(function (radio, index) {
                if (radio.id == radioId) {
                    radios.current = index;
                    return true;
                }
            });
        }
    }

    function scrollToTop() {
        $ionicScrollDelegate.scrollTop();
    };

    function onStrukStarting() {
        clearStatus();
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
        clearStatus();
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
        vm.showOnlyFav = !!userDataService.getFavPref();
        if (vm.showOnlyFav === true && getFavContextRadios().length === 0) {
            // when fav is selcted but not radios on fav on start up - switch to main list
            toggleNavFav();
        }
        
        BackgroundAudioPlayer.getStatus(function (status) {
            vm.isStopped = !(status == 1);
            $scope.$apply();
        }, function () { });

        BackgroundAudioPlayer.getVolume(function (volume) {
            vm.volume = Number.parseFloat(volume) * 100;
            $scope.$apply();
        }, function () { });

        BackgroundAudioPlayer.getCurrentRadio(function (radioId) {
            if (radioId) {
                vm.currentRadio = getAndSyncCurrentRadioById(radioId);
                $scope.$apply();
            }
        }, null);

        getTimeToClose();
    }

    function waitOnBuffering() {
        mediaBufferingPromise = $interval(checkStatusOnBuffering, 1000);
        function checkStatusOnBuffering() {
            BackgroundAudioPlayer.getStatus(function (status) {
                vm.isStopped = !(status == 1);
                if (!vm.isStopped) {
                    clearStatus();
                    if (mediaBufferingPromise) {
                        $interval.cancel(mediaBufferingPromise);
                    }
                }
            }, function () { })
        }
    }

    function getRadios() {
        alertService.showLoading();
        return radioDataService.all()
        .then(function (response) {
            vm.radios = response.data.radios;
            vm.radios.forEach(function (item) {
                item.initial = getThumpInitials(item.name);
                item.color = getRandomThumpIconColor();
            });

            var favRadios = (userDataService.getFavRadios() || '').split(',');
            favRadios.forEach(function (item) {
                vm.radios.forEach(function (radio) {
                    if (radio.id == item) radio.fav = true;
                });
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
    function augmentArrayPrototype() {
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
    }

    function updateStatus(status) {
        vm.currentStatus = status;
    }

    function clearStatus() {
        vm.currentStatus = null;
    }

    function getRandomThumpIconColor() {
        var colors = ["#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#009688", "#FF5722", "#795548", "#607D8B", "#43A047"];
        var colorIndex = Math.floor(Math.random() * 10);
        return colors[colorIndex];
    }

    function getThumpInitials(radioName) {
        var result = "";
        var parts = radioName.split(" ");
        result = result + parts[0].charAt(0);

        if (parts.length > 1) {
            result = result + parts[1].charAt(0);
        }
        return result;
    }
    // #endregion



    // #region Timerpopup

    function getTimeToClose() {
        // get remaining time from any in-progress scheduled close
        BackgroundAudioPlayer.getTimeToScheduledClose(function (closingAt) {
            if (closingAt) {
                var diffInMilli = parseInt(closingAt);
                if (diffInMilli) {
                    vm.timerSettings.duration = Math.ceil(diffInMilli / (1000 * 60));
                    $scope.$apply();
                }
            }
        }, function () { });
    }

    // Triggered on a button click, or some other target
    function showTimerPopup() {
        getTimeToClose();
        showPopupInternal();
        function showPopupInternal() {
            // An elaborate, custom popup
            var myPopup = $ionicPopup.show({
                template: '<div class="range clear-padding">' +
                          '     <i class="icon ion-ios-timer-outline"></i>' +
                          '     <input type="range" name="volume" ng-model="vm.timerSettings.duration">' +
                          '     <i class="icon ion-ios-timer"></i>' +
                          '</div>' +
                          '<div class="margin-left-10">In {{vm.timerSettings.duration}} minutes.</div>',
                title: 'Off Timer',
                scope: $scope,
                buttons: [
                  { text: 'Cancel' },
                  {
                      text: 'Reset',
                      onTap: function (e) {
                          BackgroundAudioPlayer.cancelScheduledClose(null, null);
                          vm.timerSettings.duration = 0;
                          $scope.$apply();
                          return;
                      }
                  },
                  {
                      text: 'OK',
                      type: 'button button-assertive',
                      onTap: function (e) {
                          if (!vm.timerSettings.duration) {
                              e.preventDefault();
                          } else {
                              if (vm.timerSettings.duration > 0) {
                                  BackgroundAudioPlayer.scheduleClose(function () {
                                      vm.isStopped = true;
                                      vm.timerSettings.duration = 0;
                                      $scope.$apply();
                                  }, null, vm.timerSettings.duration);
                              } else {
                                  BackgroundAudioPlayer.cancelScheduledClose(null, null);
                              }

                              return vm.timerSettings.duration;
                          }
                      }
                  }
                ]
            });
        }

    };
    // #endregion

})
.controller('aboutController', function ($scope) { });

