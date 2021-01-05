
Feature: Openid connect Scopes

Background:
* def mainUrl = scopes_url
* def getToken =
"""
function(path,method) {
//print(' path = '+path+' , method = '+method);
var result = karate.call('classpath:token.feature',{ pathUrl: path, methodName: method});
//print(' result.response after call = '+result.response);
var token = result.response
  return token;
}
"""   

Scenario: Fetch all openid connect scopes without bearer token
Given url mainUrl
When method GET
Then status 401


Scenario: Fetch all scopes
Given url mainUrl
And def accessToken = getToken(mainUrl,'GET')
And header Authorization = 'Bearer ' + accessToken
When method GET
Then status 200
And print response
And assert response.length != null


Scenario: Fetch all openid connect scopes
Given url mainUrl
And def accessToken = getToken(mainUrl,'GET')
And  header Authorization = 'Bearer ' + accessToken
And param type = 'openid'
When method GET
Then status 200
And print response
And assert response.length != null


Scenario: Fetch the first three openidconnect scopes
Given url mainUrl
And def accessToken = getToken(mainUrl,'GET')
And header Authorization = 'Bearer ' + accessToken
And param type = 'openid'
And param limit = 3
When method GET
Then status 200
And print response
And assert response.length == 3


Scenario: Search openid connect scopes given a serach pattern
Given url mainUrl
And def accessToken = getToken(mainUrl,'GET')
And  header Authorization = 'Bearer ' + accessToken
And param type = 'openid'
And param pattern = 'openid'
When method GET
Then status 200
And print response
And assert response.length == 1


@CreateUpdateDelete
Scenario: Create new OpenId Connect Scope
Given url mainUrl
And def accessToken = getToken(mainUrl,'POST')
And header Authorization = 'Bearer ' + accessToken
And request read('scope.json')
When method POST
Then status 201
And print response
Then def result = response
Then set result.displayName = 'UpdatedQAAddedScope'
Then def inum_before = result.inum
Given url mainUrl
And def accessToken = getToken(mainUrl,'PUT')
And header Authorization = 'Bearer ' + accessToken
And request result
When method PUT
Then status 200
And print response
And assert response.displayName == 'UpdatedQAAddedScope'
And assert response.inum == inum_before
Given url mainUrl + '/' +response.inum
And def accessToken = getToken(mainUrl,'DELETE')
And header Authorization = 'Bearer ' + accessToken
When method DELETE
Then status 204


Scenario: Delete a non-existing openid connect scope by inum
Given url mainUrl + '/1402.66633-8675-473e-a749'
And def accessToken = getToken(mainUrl,'GET')
And header Authorization = 'Bearer ' + accessToken
And param type = 'openid'
When method GET
Then status 404
And print response


Scenario: Get an openid connect scope by inum(unexisting scope)
Given url mainUrl + '/53553532727272772'
And def accessToken = getToken(mainUrl,'GET')
And header Authorization = 'Bearer ' + accessToken
And param type = 'openid'
When method GET
Then status 404
And print response


Scenario: Get an openid connect scopes by inum
Given url mainUrl
And def accessToken = getToken(mainUrl,'GET')
And header Authorization = 'Bearer ' + accessToken
And param type = 'openid'
When method GET
Then status 200
And print response
Given url mainUrl + '/' +response[0].inum
And def accessToken = getToken(mainUrl,'GET')
And header Authorization = 'Bearer ' + accessToken
And param type = 'openid'
When method GET
Then status 200
And print response