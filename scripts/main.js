var myapp = angular.module('myapp', ['ngRoute','AllCtr']);
        myapp.config(['$routeProvider',
          function ($routeProvider) {
              $routeProvider.
                when('/', {
                    templateUrl: 'bank.html',
                    controller: 'ChangeController'
                }).
				when('/bank',{
					templateUrl: 'bank.html',
                    controller: 'ChangeController'
				}).
				when('/drugStore',{
					templateUrl: 'drugStore.html',
                    controller: 'drugStoreController'
				}).
                otherwise({
                    redirectTo: '/'
                });
          }]);
        var ctr = angular.module('AllCtr',[]);
        ctr.controller('ChangeController', ['$scope', '$http', function ($scope, $http) { 
			$scope.m_q='1';
            $scope.money = [];
            $scope.moneyRate = {};            
            $scope.changeRate = function (rate) {
                $scope.m_p = $scope.moneyRate[rate];                
                return $scope.moneyRate[rate];                
            };
            
            $http.get('/bank/proxy.php?url=http://www.banque-centrale.mg').success(function (data,status) {
				if($(data).find('.xdebug-error').length!=0){						
					$scope.api = function () {
						return 'Cannot connect to Madagascar Central Bank';
					};					
				}
				else{
					var table = $(data).find('table').get(6);//1ère table contenant les devises
					var tr = $(table).find('tr').get(0);//1ère tr
					var td = $(tr).find('td').get(0);//1ère td
					var date = $(td).find('strong').get(0);//1ère strong dans 1ère td
					var context = $(td).find('table').get(0);//1ère table dans 1ère td				
					$(context).find('tr').each(function (i_, tr_) {
						var temp='';
						$(tr_).find('td').each(function (i__, td_) {
							if(i__==0){
								temp=$(td_).text();                            							
								$scope.money[i_] = temp.trim();								
								//$('#m_c').append('<option value="' + $scope.money[i_] + '">' + $scope.money[i_] + '</option>');							
							}
							else{							
								$scope.moneyRate[temp] = $(td_).text().split(' ').join('').replace(',','.');
							}                        
						});
					});
					$scope.m_c='EUR';
					//$('#tb').append($(context).html());
					
				}
				$('.ajax').hide();
				$('.loading').show();
                				
            }).error(function (status) {
				$('.ajax').hide();
				$('.loading').show();
                $scope.money = ['EUR','USD'];
                $scope.moneyRate = {'EUR':3125.4,'USD':2300};
                $scope.api = function () {
                    return 'Cannot connect to Madagascar Central Bank';
                };				
            });
        }]);       
		ctr.controller('drugStoreController',['$scope','$http',function($scope,$http){
			$scope.m_welcome='En cours d\'extraction';
			$http.get('/bank/proxy.php?url=http://ordrepharmacien.mg/recherche-pharmacie-de-garde/').success(function(data){
				if($(data).find('.xdebug-error').length!=0){						
					$scope.m_welcome='Cannot connect to Ordre des pharmaciens de Madagascar';				
				}
				else{				
					$scope.m_welcome=undefined;
					$scope.m_pharmacies=[];
					var table=$(data).find('#table_pharm')[0];
					var date=$(data).find('h3')[0];
					$scope.m_date=date.innerText;
					$(table).find('tr').each(function(index,val){
						if(index!=0){
							var tds=$(val).find('td');
							
							$scope.m_pharmacies.push({
								'Name':$(tds.get(0)).text(),
								'Adresse':$(tds.get(1)).text(),
								'Contact':$(tds.get(2)).text()
							});
						}
					});
				}				
				$('.loading').hide();
			}).
			error(function(){
			
			});
		}]);