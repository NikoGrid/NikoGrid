@REQ_NIK-5
Feature: Slot Booking and Scheduling

	#Tests As a registered user, I want to cancel a reservation I no longer need, so that I can free the charging spot.
	@TEST_NIK-388 @REQ_NIK-25
	Scenario: Test Cancel reservation
		Given I have the account with email "test@test.com" and password "password"
		And the following charging stations exist:
			| name      | latitude | longitude | availability |
			| Station 1 | 0.0      | 0.0       | FREE         |
		And the following reservations are booked:
		  | startsAt  | endsAt   |
		  | 50        | 51       |
		  | 52        | 53       |
		  | -22       | -20      |
		  | -49       | -48      |
		When I open the application
		And I am authenticated
		And I go to my profile
		Then I should see 4 reservations
		And when I cancel the first reservation
		Then I should see 3 reservations
		
