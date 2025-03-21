# Sojourner under Sabotage - Server
## Create the Unity build
Build the Unity project with WebGL as target platform. Use `/src/main/resources/static/unity/` as build output directory.  
The source code or a prebuilt export can be found in the Sojourner-under-Sabotage-Game repository.

## Webpack Install and Build
- switch to `/src/main/js/`
- install dependencies `npm install`
- execute webpack `npm run build`

## Install and Run the Server
- Install dependencies `mvn install`
- Rename `example.application.properties` to `application.properties` and add your database credentials
- Run the application `mvn spring-boot:run`

Now the server runs and the frontend can be accessed at the port configured in the `application.properties`.


## Test the API
```shell
curl localhost:8080/api/hello -i
```


```shell
curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST localhost:8080/api/auth -d  "{\"login\":\"user@mail.com\",\"password\":\"1234\"} -i
````

```shell
curl -H "Accept: application/json" -H "Content-Type: application/json" -X POST -d "{\"className\":\"DemoTest\",\"sourceCode\":\"public class DemoTest{@org.junit.Test public void testDemoAddition(){org.junit.Assert.assertEquals(3, Demo.add(2, 1));}}\",\"cutComponentName\":\"Demo\"}" -i -H "Authorization: Bearer insert_valid_token_here" localhost:8080/api/execute
```
