@REQ_NIK-9
Feature: Station Management

	@TEST_NIK-402 @REQ_NIK-20
	Scenario: Test Register a station
		Given I have the admin account
		When I open the application
        And I am authenticated as admin
		And I click the button to create a new station
		Then I see a location creation form
		And when I input the name 'A', the latitude 0 and the longitude 0
		And I browse for stations at 0.0, 0.0
		Then I get the stations:
		  | name      |
		  | A         |