@REQ_NIK-4
Feature: Station Discovery

	#As a user, I want to filter for active and available stations, so that I can book a slot and charge my electric vehicles.
	@TEST_NIK-35 @REQ_NIK-11
	Scenario: Test Filter charging stations
		Given the following charging stations exist:
		  | name      | latitude  | longitude | availability  |
		  | Station 1 | 0.0       | 0.0       | BOOKED        |
		  | Station 2 | 0.01      | 0.0       | BOOKED        |
		  | Station 3 | 0.0       | 0.01      | FREE          |
		When I open the application
		And I filter for stations with availability FREE
		And I browse for stations at 0.0, 0.0
		Then I get the stations:
		  | name      |
		  | Station 3 |
		
