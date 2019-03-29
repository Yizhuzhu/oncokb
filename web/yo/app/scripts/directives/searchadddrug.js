'use strict';
angular.module('oncokbApp')
    .directive('searchAddDrug', function (DatabaseConnector, dialogs, _, mainUtils, $q, FirebaseModel, firebaseConnector) {
        return {
            templateUrl: 'views/searchAddDrug.html',
            restrict: 'E',
            controller: function ($scope) {
                //updateDrugs();
                function checkSameDrug(drugName, code) {
                    return _.some(mainUtils.getKeysWithoutFirebasePrefix($scope.drugList), (key) => ((code === '' && drugName === $scope.drugList[key].drugName) || (code !== '' && code === $scope.drugList[key].ncitCode)) === true);
                }

                function createDrug(drugName, ncitCode, synonyms, ncitName) {
                    ncitCode = undefinedToEmptyString(ncitCode);
                    synonyms = undefinedToEmptyString(synonyms);
                    ncitName = undefinedToEmptyString(ncitName);
                    var deferred = $q.defer();
                    if (($scope.drugList === undefined) || (checkSameDrug(drugName, ncitCode) === false)) {
                        var drug = new FirebaseModel.Drug(drugName, ncitCode, synonyms, ncitName);
                        firebaseConnector.addDrug(drug.uuid, drug).then(function (result) {
                            deferred.resolve();
                            $scope.addDrugMessage = drugName + " has been added successfully.";
                        }, function (error) {
                            $scope.addDrugMessage = 'Failed to create the drug ' + drugName + '! Please contact developers.';
                            deferred.reject(error);
                        });
                    }
                    else {
                        $scope.addDrugMessage = "Sorry, same drug exists.";
                        $scope.suggestedDrug = '';
                        $scope.preferName = '';
                    }
                    return deferred.promise;
                }

                function undefinedToEmptyString(item) {
                    if(!item){
                        item = '';
                    }
                    return item;
                }

                function updateDrugs() {
                    // var list = ["C62035",
                    //     "C48375",
                    //     "C38713",
                    //     "C114494",
                    //     "C102564",
                    //     "C61948",
                    //     "C71721",
                    //     "C98283",
                    //     "C84865",
                    //     "C1723",
                    //     "C68923",
                    //     "C113330",
                    //     "C80059",
                    //     "C95733",
                    //     "C65530",
                    //     "C1855",
                    //     "C66940",
                    //     "C53398",
                    //     "C98838",
                    //     "C26653",
                    //     "C122834",
                    //     "C125657",
                    //     "C88302",
                    //     "C88272",
                    //     "C116722",
                    //     "C1872",
                    //     "C1703",
                    //     "C114383",
                    //     "C111573",
                    //     "C78204",
                    //     "C107384",
                    //     "C1844",
                    //     "C66939",
                    //     "C68814",
                    //     "C2654",
                    //     "C94214",
                    //     "C90565",
                    //     "C116876",
                    //     "C96796",
                    //     "C132166",
                    //     "C74038",
                    //     "C106261",
                    //     "C107684",
                    //     "C134987",
                    //     "C132295",
                    //     "C2737",
                    //     "C129546",
                    //     "C60809",
                    //     "C95777",
                    //     "C642",
                    //     "C933",
                    //     "C101259",
                    //     "C422",
                    //     "C46089",
                    //     "C1127",
                    //     "C74061",
                    //     "C115112",
                    //     "C101790",
                    //     "C113655",
                    //     "C98831",
                    //     "C82386",
                    //     "C64768",
                    //     "C77908",
                    //     "C1857",
                    //     "C137800",
                    //     "C49176",
                    //     "C97660",
                    //     "C95701",
                    //     "C62091",
                    //     "C116377",
                    //     "C49094",
                    //     "C82492",
                    //     "C1647",
                    //     "C82492",
                    //     "C38692",
                    //     "C376",
                    //     "C1379",
                    //     "C111988",
                    //     "C103273",
                    //     "C64639",
                    //     "C105402",
                    //     "C68936",
                    //     "C15807",
                    //     "C77888",
                    //     "C107506",
                    //     "C156804",
                    //     "C71622",
                    //     "C123827",
                    //     "C52200",
                    //     "C90564",
                    //     "C48387",
                    //     "C74052",
                    //     "C115977",
                    //     "C114984",
                    //     "C106432",
                    //     "C98844",
                    //     "C82385",
                    //     "C981"];
                    var list =["C62035",
                        "C48375",
                        "C38713",
                        "C102564",
                        "C61948",
                        "C71721",
                        "C98283",
                        "C84865",
                        "C1723",
                        "C68923",
                        "C113330",
                        "C80059",
                        "C95733",
                        "C65530",
                        "C1855",
                        "C66940",
                        "C53398",
                        "C98838",
                        "C26653",
                        "C122834",
                        "C88302",
                        "C88272",
                        "C125657",
                        "C1703",
                        "C114383",
                        "C111573",
                        "C78204",
                        "C107384",
                        "C1844",
                        "C66939",
                        "C68814",
                        "C2654",
                        "C94214",
                        "C90565",
                        "C116876",
                        "C96796",
                        "C132166",
                        "C74038",
                        "C106261",
                        "C107684",
                        "C134987",
                        "C132295",
                        "C2737",
                        "C74061",
                        "C115112",
                        "C101790",
                        "C113655",
                        "C98831",
                        "C82386",
                        "C64768",
                        "C77908",
                        "C1857",
                        "C137800",
                        "C49176",
                        "C97660",
                        "C95701",
                        "C116377",
                        "C49094",
                        "C82492",
                        "C1647",
                        "C82492",
                        "C38692",
                        "C376",
                        "C1379",
                        "C111988",
                        "C103273",
                        "C77888",
                        "C107506",
                        "C156804",
                        "C71622",
                        "C123827",
                        "C52200",
                        "C90564",
                        "C48387",
                        "C74052",
                        "C115977",
                        "C114984",
                        "C106432",
                        "C98844",
                        "C82385",
                        "C91724"
                    ];
                    var tem = [];
                    _.each(list, function (code) {
                        DatabaseConnector.searchDrugs(code).then(function (result) {
                            tem = result;
                            _.each(tem, function (drug) {
                                if(drug.ncitCode === code){
                                    addThis(drug);
                                };
                            })
                            return result;
                        });
                    })
                }

                function addThis(drug) {
                    createDrug(drug.drugName, drug.ncitCode, drug.synonyms, drug.drugName).then(function (result) {
                        $scope.suggestedDrug = '';
                        $scope.preferName = '';
                    });
                }

                //createDrug("GSK343", "C156804", ["Enhancer of Zeste Homolog 2 Inhibitor", "EZH2 Inhibitor"], "EZH2 Inhibitor");


                $scope.addDrug = function (drug, preferName) {
                    if(drug !== ''){
                        if (!drug.ncitCode) {
                            preferName = drug;
                            createDrug(preferName).then(function (result) {
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
