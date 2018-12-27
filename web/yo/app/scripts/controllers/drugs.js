'use strict';

angular.module('oncokbApp')
    .controller('DrugsCtrl', ['$window', '$scope', '$rootScope', '$location', '$timeout', '$routeParams', '_', 'DTColumnDefBuilder', 'DTOptionsBuilder', '$firebaseObject', '$firebaseArray', 'FirebaseModel', '$q', 'dialogs',
        function ($window, $scope, $rootScope, $location, $timeout, $routeParams, _, DTColumnDefBuilder, DTOptionsBuilder, $firebaseObject, $firebaseArray, FirebaseModel, $q, dialogs) {
            function LoadDrugTable() {
                var deferred1 = $q.defer();
                $firebaseObject(firebase.database().ref("Drugs/")).$bindTo($scope, "drugList").then(function () {
                    deferred1.resolve();
                }, function (error) {
                    deferred1.reject(error);
                });
                var deferred2 = $q.defer();
                $scope.drugMap = {};
                firebase.database().ref('Map').once('value').then(function (snapshot) {
                    var mapList = snapshot.val();
                    var drugUuids = _.without(_.keys(mapList));
                    _.each(drugUuids, function (drug) {
                        $scope.drugMap[drug] = [];
                        var genes = _.without(_.keys(mapList[drug]));
                        _.each(genes, function (gene) {
                            var mutations = _.without(_.keys(mapList[drug][gene]));
                            var mapInformation = {
                                geneName: gene,
                                geneLink: "#!/gene/" + gene,
                                mutationNumber: mutations.length,
                                mutationInfo: mutations
                            };
                            $scope.drugMap[drug].push(mapInformation);
                        });
                    });
                    deferred2.resolve();
                }, function (error) {
                    deferred2.reject(error);
                    console.log((error));
                });
                var bindingAPI = [deferred1.promise, deferred2.promise];
                $q.all(bindingAPI)
                    .then(function (result) {

                    }, function (error) {
                        console.log('Error happened', error);
                    });

            }

            LoadDrugTable();

            function checkSameName(newDrugName, uuid) {
                var sameName = false;
                var drugKeys = _.without(_.keys($scope.drugList), "$id", "$priority");
                _.some(drugKeys, function (key) {
                    if ($scope.drugList[key].uuid !== uuid && (newDrugName === $scope.drugList[key].drugName || (($scope.drugList[key].synonyms !== undefined) && ($scope.drugList[key].synonyms.indexOf(newDrugName) > -1)))) {
                        sameName = true;
                        return true;
                    }
                    ;
                });
                return sameName;
            };

            function modalError(errorTitle, errorMessage, deleteDrug, drug) {
                var dlgfortherapy = dialogs.create('views/modalError.html', 'ModalErrorCtrl', {
                        errorTitle: errorTitle,
                        errorMessage: errorMessage,
                        deleteDrug: deleteDrug,
                        drug: drug
                    },
                    {
                        size: 'sm'
                    });
            };
            $scope.saveDrugName = function (newDrugName, drug) {
                if (checkSameName(newDrugName, drug.uuid)) {
                    modalError("Sorry", "Same name exists.", false);
                } else {
                    if ((newDrugName === '') || (newDrugName == null))
                        newDrugName = drug.drugName;
                    firebase.database().ref('Drugs/' + drug.uuid + '/drugName').set(newDrugName);
                }
                ;
            };
            $scope.removeDrug = function (drug) {
                firebase.database().ref('Map').once('value', function (snapshot) {
                    if (snapshot.hasChild(drug.uuid)) {
                        modalError("Sorry", "Can't delete this drug, because it is used in therapies.", false)
                    }
                    else {
                        modalError("Attention", drug.drugName + " will be deleted.", true, drug);
                    }
                });
            }
        }]
    )
    .controller('ModalErrorCtrl', function ($scope, $modalInstance, data) {
        $scope.errorTitle = data.errorTitle;
        $scope.errorMessage = data.errorMessage;
        $scope.deleteDrug = data.deleteDrug;
        $scope.cancel = function () {
            $modalInstance.dismiss('canceled');
        };
        $scope.confirm = function () {
            firebase.database().ref('Drugs/' + data.drug.uuid).set(null).then();
            $modalInstance.dismiss('canceled');
        }
    });


