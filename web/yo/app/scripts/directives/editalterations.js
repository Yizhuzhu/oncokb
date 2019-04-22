'use strict';
angular.module('oncokbApp')
    .directive('editAlteration', function ($rootScope, DatabaseConnector, _, $q, FirebaseModel, firebaseConnector, drugMapUtils, $window, mainUtils) {
        return {
            templateUrl: 'views/editAlterations.html',
            restrict: 'E',
            scope:{
                unvalidMutations: '=',
                mutationArray: '=',
                newMutationName: '=',
                alterations: '=',
                //saveMutationCallback: '&addMutation',
            },
            controller: function ($scope) {
                function getComponentsOfAlteration(alteration, index){
                    $scope.mutationArray[index] = alteration.alterationInput;
                    $scope.newMutationName = $scope.mutationArray.toString(', ');
                    alteration.refResidues = alteration.alterationInput.substring(0,1);
                    var i = 1;
                    while(i<alteration.alterationInput.length && mainUtils.isNumber(alteration.alterationInput.substring(i,i+1))){
                        i++
                    }
                    alteration.proteinStart = alteration.alterationInput.substring(1,i);
                    alteration.variantResidues = alteration.alterationInput.substring(i);
                }
                function updateAlterations(alteration, index){
                    alteration.alterationInput = alteration.refResidues + alteration.proteinStart + alteration.variantResidues;
                    $scope.mutationArray[index] = alteration.alterationInput;
                    $scope.newMutationName = $scope.mutationArray.toString(', ');
                }
                // function isLetter(c){
                //     return ((c>='a' && c<='z') || (c>='A' && c<='Z'));
                // }
                $scope.refreshAlterationInput = function(sourceType, alteration, index){
                    switch (sourceType){
                        case 'refResidues':
                        case 'proteinStart':
                        case 'proteinEnd':
                        case 'variantResidues':
                            updateAlterations(alteration, index);
                            break;
                        case 'alterationInput':
                            getComponentsOfAlteration(alteration, index);
                            break;

                        default:
                            break;
                    }
                };
                $scope.removeAlteration = function(index){
                    $scope.alterations.splice(index, 1);
                    $scope.mutationArray.splice(index, 1);
                    $scope.newMutationName = $scope.mutationArray.toString(', ');
                };
                $scope.addAlteration = function(){
                    $scope.alterations.push({
                        alterationInput: '',
                        refResidues: '',
                        proteinStart: '',
                        variantResidues: '',
                        valid: true
                    })
                };
                // $scope.addMutation = function (newMutationName) {
                //     $scope.saveMutationCallback({
                //         newMutationName: newMutationName
                //     })
                // }
            }
        }
    });
