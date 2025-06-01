@REQ_NIK-4
Feature: Station Discovery

	#Tests As a user, I want to browse nearby stations, so that I can make a more informed decision on my next car purchase.
	@TEST_NIK-38 @REQ_NIK-37
	Scenario: Test Browse nearby charging stations
		Given the following charging stations exist:
		  | name      | latitude  | longitude  |
		  | Station 1 | 0.0       | 0.0        |
		  | Station 2 | 1.0       | 1.0        |
		  | Station 3 | 45.0      | 45.0       |
		When I open the application
		 And I browse for stations at 0.5, 0.5
		Then I get the stations:
		  | name      |
		  | Station 1 |
		  | Station 2 |

	#As a user, I want to find the closest available charging location to my or another location, so that I can head there to charge.
	@TEST_NIK-39 @REQ_NIK-24
	Scenario: Test Find closest available charging station
		Given the following charging stations exist:
			| name      | latitude  | longitude  |
			| Station 1 | 0.0       | 0.0        |
			| Station 2 | 1.0       | 1.0        |
			| Station 3 | 45.0      | 45.0       |
			| Station 4 | 40.6      | -8.6       |
		When I open the application
		 And I browse for the closest station to 'Aveiro, Portugal'
		Then I get the 'Station 4'
		
