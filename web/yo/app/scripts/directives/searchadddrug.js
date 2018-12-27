'use strict';

angular.module('oncokbApp')
    .directive('searchAddDrug', function ($rootScope, DatabaseConnector, dialogs, _, $q, FirebaseModel) {
        return {
            templateUrl: 'views/searchAddDrug.html',
            restrict: 'AE',
            controller: function ($scope) {
                function checkSame(drugName, code) {
                    var isSame = false;
                    var drugKeys = _.without(_.keys($scope.drugList), "$id", "$priority", "$value");
                    _.each(drugKeys, function (key) {
                        if (((code === '') || (code === null)) && (drugName === $scope.drugList[key].drugName)) {
                            isSame = true;
                            return isSame;
                        }
                        else if (($scope.drugList[key].ncitCode !== '') && (code === $scope.drugList[key].ncitCode)) {
                            isSame = true;
                            return isSame;
                        }
                    })
                    return isSame;
                };

                function createDrug(drugName, ncitCode, synonyms, ncitName) {
                    var deferred = $q.defer();
                    var drug = new FirebaseModel.Drug(drugName, ncitCode, synonyms, ncitName);
                    if (($scope.drugList === undefined) || (checkSame(drugName, ncitCode) == false)) {
                        firebase.database().ref('Drugs/' + drug.uuid).set(drug).then(function (result) {
                            deferred.resolve();
                        }, function (error) {
                            console.log(error);
                            dialogs.notify('Warning', 'Failed to create the drug ' + drugName + '!');
                            deferred.reject(error);
                        });
                        $scope.addDrugMessage = drugName + " has been added successfully.";
                    }
                    else {
                        $scope.addDrugMessage = "Sorry, same drug exists.";
                        $scope.suggestedDrug = '';
                        $scope.preferName = '';
                    }
                    return deferred.promise;
                }

                $scope.addDrug = function (drug, preferName) {
                    if ((drug.ncitCode == null) || (drug.ncitCode == '')) {
                        preferName = drug;
                        createDrug(preferName, '', '', '').then(function (result) {
                                $scope.suggestedDrug = '';
                                $scope.preferName = '';
                                $scope.addDrugMessage = '';
                            },
                            function (error) {
                                console.log("add unsuccessfully")
                            });
                    }
                    else {
                        if ((preferName == '') || (preferName == null)) {
                            preferName = drug.drugName;
                        }
                        createDrug(preferName, drug.ncitCode, drug.synonyms, drug.drugName).then(function (result) {
                                $scope.suggestedDrug = '';
                                $scope.preferName = '';
                            },
                            function (error) {
                                console.log("add unsuccessfully.")
                            });
                    }
                }

                $scope.processSearchDrugs = function (keyword) {
                    return DatabaseConnector.searchDrugs(keyword)
                        .then(
                            function (result) {
                                $scope.searchDrugsError = false;
                                return result;
                            })
                        .catch(
                            function (error) {
                                $scope.searchDrugsError = true;
                            }
                        )
                };

            }
        };
    });
