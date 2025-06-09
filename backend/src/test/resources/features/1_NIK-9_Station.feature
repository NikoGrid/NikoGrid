@REQ_NIK-9
Feature: Station Management

	@TEST_NIK-403 @REQ_NIK-20
	Scenario: Test Register a station
		Given I have the admin account
		And the following charging stations exist:
		  | name      | latitude | longitude | availability |
		  | Station 1 | 10.10    | 10.10     | FREE         |
		When I open the application
		And I am authenticated as admin
		And I browse for the station closest to 10.10, 10.10
		And I select a station
		And I click the button to create a new station
		Then I see a station creation form
		And when I input the name 'A' and the power 250
		And when I submit the form to create a station
		Then I get confirmation that the station was created
		
