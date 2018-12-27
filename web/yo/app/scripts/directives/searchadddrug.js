'use strict';

angular.module('oncokbApp')
    .directive('searchAddDrug', function (DatabaseConnector, dialogs, _, mainUtils, $q, FirebaseModel) {
        return {
            templateUrl: 'views/searchAddDrug.html',
            restrict: 'E',
            controller: function ($scope) {
                function checkSameDrug(drugName, code) {
                    return _.some(mainUtils.getKeysWithoutFirebasePrefix($scope.drugList), (key) => ((code === '' && drugName === $scope.drugList[key].drugName) || (code !== '' && code === $scope.drugList[key].ncitCode)) === true);
                };

                function createDrug(drugName, ncitCode, synonyms, ncitName) {
                    var deferred = $q.defer();
                    if (($scope.drugList === undefined) || (checkSameDrug(drugName, ncitCode) === false)) {
                        var drug = new FirebaseModel.Drug(drugName, ncitCode, synonyms, ncitName);
                        firebase.database().ref('Drugs/' + drug.uuid).set(drug).then(function (result) {
                            deferred.resolve();
                        }, function (error) {
                            dialogs.notify('Warning', 'Failed to create the drug ' + drugName + '! Please contact developers.');
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
                };

                $scope.addDrug = function (drug, preferName) {
                    if(drug !== ''){
                        if (!drug.ncitCode) {
                            preferName = drug;
                            createDrug(preferName, '', '', '').then(function (result) {
                                    $scope.suggestedDrug = '';
                                    $scope.preferName = '';});
                        }
                        else {
                            if (!preferName) {
                                preferName = drug.drugName;
                            }
                            createDrug(preferName, drug.ncitCode, drug.synonyms, drug.drugName).then(function (result) {
                                    $scope.suggestedDrug = '';
                                    $scope.preferName = '';});
                        }
                    }
                };

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
                                return [];
                            }
                        )
                };

            }
        };
    });
