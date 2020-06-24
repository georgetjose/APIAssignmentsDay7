package tomTom;

import java.util.List;
import java.util.Random;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class GeoFencing 
{
	String apiKey, adminKeyBody, adminKey,projectCreationBody, projectId, fenceCreationBody, fenceId;
	Random random = new Random(); 
	int randomNumber,count1=0,count2=0;
	@BeforeSuite
	void setEnvironment()
	{
		apiKey ="=NLAwuC1K8afD3B557WsMdI84oV6BowVz";
		adminKeyBody ="{\"secret\":\"gtjgeofencing\"}";
		
		randomNumber = random.nextInt(100000);
		
		projectCreationBody = "{ \"name\": \"GTJProject"+randomNumber+"\"}";
		
		fenceCreationBody ="{\r\n" + 
				"  \"name\": \"My Area "+randomNumber+"\",\r\n" + 
				"  \"type\": \"Feature\",\r\n" + 
				"  \"geometry\": {\r\n" + 
				"    \"radius\": 100,\r\n" + 
				"    \"type\": \"Point\",\r\n" + 
				"    \"shapeType\": \"Circle\",\r\n" + 
				"    \"coordinates\": [10.340435, 76.280886]\r\n" + 
				"  },\r\n" + 
				"  \"properties\": {\r\n" + 
				"    \"maxSpeedKmh\": 90\r\n" + 
				"  }\r\n" + 
				"}";
		
		
		RestAssured.baseURI = "https://api.tomtom.com/geofencing/1";
	}
	
	@BeforeMethod
	void generateAdminKey()
	{
		Response postAdminKey = RestAssured
								.given()
								.contentType(ContentType.JSON)
								.body(adminKeyBody)								
								.post("regenerateKey?key="+apiKey);
		
		JsonPath postAdminKeyJson = postAdminKey.jsonPath();
		adminKey = postAdminKeyJson.getString("adminKey");
		if(postAdminKey.getStatusCode()==200)
			System.out.println("The admin Key: "+adminKey+" is generated successfully !!!");
		else
			System.err.println("The admin Key couldn't be generated !!!");
		System.out.println("*****************************************************************************************");
	}
	
	@Test(priority=1)
	void createProjectAndVerify()
	{		
		Response postProject = RestAssured
				.given()
				.contentType(ContentType.JSON)
				.body(projectCreationBody)
				.post("projects/project?key="+apiKey+"&adminKey="+adminKey);
		
		JsonPath postProjectJson = postProject.jsonPath();
		projectId = postProjectJson.getString("id");
		
		
		if(postProject.getStatusCode()!=201)
			System.err.println("The Project couldn't be created!!!");
		else
		{
			System.out.println("The Project is created successfully with id: "+projectId);
		
			Response getProjects = RestAssured
								   .given()
								   .contentType(ContentType.JSON)
								   .get("projects?key="+apiKey);
		
			JsonPath getProjectsJson = getProjects.jsonPath();
			List<String> allprojects = getProjectsJson.getList("projects");
			for(int i=0;i<allprojects.size();i++)
			{
				if(getProjectsJson.getString("projects["+i+"].id").equals(projectId))
				{
					System.out.println("The created Project is available in the list!!");
					count1++;
					break;
				}
			}
			if(count1==0)
				System.err.println("The created Project is not available in the list!!");
		}
		System.out.println("*****************************************************************************************");
	}
	
	@Test(priority=2,dependsOnMethods= {"createProjectAndVerify"})
	void createFenceAndVerify()
	{
		Response postFence = RestAssured
							 .given()
							 .contentType(ContentType.JSON)
							 .body(fenceCreationBody)
							 .post("projects/"+projectId+"/fence?key="+apiKey+"&adminKey="+adminKey);
		
		JsonPath postFenceJson = postFence.jsonPath();
		fenceId = postFenceJson.getString("id");
		
		if(postFence.getStatusCode()!=201)
			System.err.println("The Fence couldn't be created!!!");
		else
		{
			System.out.println("The Fence is created successfully with id: "+fenceId);
		
			Response getFences = RestAssured
								 .given()
								 .contentType(ContentType.JSON)
								 .get("projects/"+projectId+"/fences?key="+apiKey);
		
			JsonPath getFencesJson = getFences.jsonPath();
			List<String> allfences = getFencesJson.getList("fences");
			for(int i=0;i<allfences.size();i++)
			{
				if(getFencesJson.getString("fences["+i+"].id").equals(fenceId))
				{
					System.out.println("The created Fence is available in the list!!");
					count2++;
					break;
				}
				if(count2==0)
					System.err.println("The created Fence is not available in the list!!");
			}
			
			Response getAFence = RestAssured
					 .given()
					 .contentType(ContentType.JSON)
					 .get("fences/"+fenceId+"?key="+apiKey);

			JsonPath getAFenceJson = getAFence.jsonPath();
			System.out.println("The details of the newly created Fence are mentioned below:");
			System.out.println("-->Name: "+getAFenceJson.getString("name"));
			System.out.println("-->Coordinates: "+getAFenceJson.getString("geometry.coordinates[0]")+", "+getAFenceJson.getString("geometry.coordinates[1]"));
			System.out.println("-->Radius: "+getAFenceJson.getString("geometry.radius"));
			System.out.println("-->Shape: "+getAFenceJson.getString("geometry.shapeType"));
		}
		
		
		System.out.println("*****************************************************************************************");
	}

}
