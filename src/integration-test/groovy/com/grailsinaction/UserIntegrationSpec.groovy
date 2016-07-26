package com.grailsinaction


import grails.test.mixin.integration.Integration
import grails.transaction.*
import spock.lang.*

@Integration
@Rollback
class UserIntegrationSpec extends Specification {

    def "Save our first user to the database"() {
        given: 'A brand new user'
        def joe = new User(loginId: 'joe', password: 'secret', homepage: 'http://grailsinaction.com')

        when: 'the user is saved'
        joe.save()

        then: 'it saved successfully and can be found in the database'
        joe.errors.errorCount == 0
        joe.id != null
        User.get(joe.id).loginId == joe.loginId
    }

    def "Updating a saved user changes its properties"() {
        given: 'An existing user'
        def existingUser = new User(loginId: 'joe', password: 'secret', homepage: 'http://grailsinaction.com')
        existingUser.save(failOnError: true)

        when: 'a property is changed'
        def foundUser = User.get(existingUser.id)
        foundUser.password = 'sesame'
        foundUser.save(failOnError: true)

        then: 'the change is reflected in the database'
        User.get(existingUser.id).password == 'sesame'
    }

    def "Deleting an existing user removes it from the database"() {
        given: 'An existing user'
        def existingUser = new User(loginId: 'joe', password: 'secret', homepage: 'http://grailsinaction.com')
        existingUser.save(failOnError: true)

        when: 'The user is deleted'
        def foundUser = User.get(existingUser.id)
        foundUser.delete(flush: true)

        then: 'The user is removed from the database'
        !User.exists(foundUser.id)
    }

    def "Saving a user with invalid properties causes an error"() {
        given: 'A user which fails several field validations'
        def user = new User(loginId: 'Joe', password: 'tiny', homepage: 'not-a-url')

        when: 'The user is validated'
        user.validate()

        then: 'The user has errors'
        user.hasErrors()

        "size.toosmall" == user.errors.getFieldError('password').code
        "tiny" == user.errors.getFieldError("password").rejectedValue
        "url.invalid" == user.errors.getFieldError("homepage").code
        "not-a-url" == user.errors.getFieldError("homepage").rejectedValue
        !user.errors.getFieldError("loginId")
    }

    def "Recovering from a save by fixing invalid properties"() {
        given: 'A user with invalid properties'
        def chuck = new User(loginId: 'chuck', password: 'tiny', homepage: 'not-a-url')
        assert chuck.save() == null
        assert chuck.hasErrors()

        when: 'We fix the invalid properties'
        chuck.password = 'fistfist'
        chuck.homepage = 'http://www.chucknorrisfacts.com'
        chuck.validate()

        then: 'The user saves and validates fine'
        !chuck.hasErrors()
        chuck.save()
    }

    def "User cannot have password that is the same as their login id"() {
        given: 'A user with a password which is the same as their login id'
        def chuck = new User(loginId: 'chuckn', password: 'chuckn', homepage: 'http://chucknorris.com')

        when: 'The user is validated'
        chuck.validate()

        then: 'The user has an error'
        chuck.hasErrors()

        chuck.errors.getFieldError('password').code == 'validator.invalid'
    }

    def "Ensure a user can follow other users"() {
        given: "A set of baseline users"
        def joe = new User(loginId: 'joe', password: 'password').save()
        def jane = new User(loginId: 'jane', password: 'password').save()
        def jill = new User(loginId: 'jill', password: 'password').save()

        when: "Joe follows Jane & Jill, and Jill follows Jane"
        joe.addToFollowing(jane)
        joe.addToFollowing(jill)
        jill.addToFollowing(jane)

        then: "Follower counts should match following people"
        joe.following.size() == 2
        jill.following.size() == 1
    }

}
