angular.module('app', ['ionic', 'app.controllers', 'app.services'])

.run(function ($ionicPlatform) {
    $ionicPlatform.ready(function () {
        // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
        // for form inputs)
        if (window.cordova && window.cordova.plugins && window.cordova.plugins.Keyboard) {
            cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
        }

        var admobid = {};
        if (/(android)/i.test(navigator.userAgent)) { // for android
            admobid = {
                banner: 'ca-app-pub-3809588371499381/5166174152', // or DFP format "/6253334/dfp_example_ad"
                interstitial: 'ca-app-pub-xxx/yyy'
            };
        } else if (/(ipod|iphone|ipad)/i.test(navigator.userAgent)) { // for ios
            admobid = {
                banner: 'ca-app-pub-xxx/zzz', // or DFP format "/6253334/dfp_example_ad"
                interstitial: 'ca-app-pub-xxx/kkk'
            };
        } else { // for windows phone
            admobid = {
                banner: 'ca-app-pub-xxx/zzz', // or DFP format "/6253334/dfp_example_ad"
                interstitial: 'ca-app-pub-xxx/kkk'
            };
        }

        // it will display smart banner at top center, using the default options
        if (AdMob) AdMob.createBanner({
            adId: admobid.banner,
            position: AdMob.AD_POSITION.BOTTOM_CENTER,
            autoShow: true
        });

        if (window.StatusBar) {
            // org.apache.cordova.statusbar required
            StatusBar.styleLightContent();

            if (/(android)/i.test(navigator.userAgent)) {
                StatusBar.backgroundColorByHexString("#FFF");
            }
        }
    });
})
.config(function ($stateProvider, $urlRouterProvider) {
    $stateProvider
    // setup an abstract state for the tabs directive
      .state('app', {
          url: "/app",
          abstract: true,
          templateUrl: "templates/menu.html"
      })

    // Each tab has its own nav history stack:

    .state('app.radio', {
        url: '/radio',
        views: {
            'content': {
                templateUrl: 'templates/radio.html',
                controller: 'radioController as vm'
            }
        }
    })

    .state('app.about', {
        url: '/about',
        views: {
            'content': {
                templateUrl: 'templates/about.html',
                controller: 'aboutController as vm'
            }
        }
    });
    $urlRouterProvider.otherwise('/app/radio');

});
