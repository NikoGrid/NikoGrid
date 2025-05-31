@REQ_NIK-40
Feature: User Profile Management

	#Tests As a user, I want to be able to log in into my account, so that I can access it from a different device.
	@TEST_NIK-260 @REQ_NIK-42
	Scenario: Test Account login
		Given I have the account with email "test@test.test" and password "test1234"
		When I open the application
		And navigate to the login page
		Then I see a login form
		And when I login with the email "test@test.test" and password "test1234"
		Then I get redirected to the home page
		
