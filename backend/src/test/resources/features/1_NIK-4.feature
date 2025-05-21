@REQ_NIK-4
Feature: Station Discovery

	#Tests As a user, I want to browse nearby stations, so that I can make a more informed decision on my next car purchase.
	@TEST_NIK-38 @REQ_NIK-37
	Scenario: Test Browse nearby charging stations
		Given the following charging stations exist:
		  | name      | latitude  | longitude |
		  | Station 1 | 0.0       | 0.0       |
		  | Station 2 | 1.0       | 1.0       |
		  | Station 3 | 45.0      | 45.0      |
		When I open the application
		 And I browse for stations at 0.5, 0.5
		Then I get the stations:
		  | name      |
		  | Station 1 |
		  | Station 2 |
		
