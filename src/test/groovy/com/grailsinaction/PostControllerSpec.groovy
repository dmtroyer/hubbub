package com.grailsinaction

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import spock.lang.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestFor(PostController)
@Mock([User,Post])
class PostControllerSpec extends Specification {

    def "Get a users timeline given their ID"() {
        given: "A user with posts in the db"
        User chuck = new User(loginId: "chuck_norris", password: "password")
        chuck.addToPosts(new Post(content: "A first post"))
        chuck.addToPosts(new Post(content: "A second post"))
        chuck.save(failOnError: true)

        and: "A loginId parameter"
        params.id = chuck.loginId

        when: "The timeline is invoked"
        def model = controller.timeline()

        then: "the user is in the returned model"
        model.user.loginId == "chuck_norris"
        model.user.posts.size() == 2

        and: "a 200 is sent to the browser"
        response.status == 200
    }

    def "Check that non-existent users are handled with an error"() {
        given: "The id of a non-existent user"
        params.id = "this-user-id-does-not-exist"

        when: "The timeline is invoked"
        controller.timeline()

        then: "a 404 is sent to the browser"
        response.status == 404
    }

    def "Adding a valid post to the timeline"() {
        given: "A user with posts in the DB"
        User chuck = new User(loginId: "chuck_norris", password: "password").save(failOnError: true)

        and: "A loginId parameter"
        params.id = chuck.loginId

        and: "Some content for the post"
        params.content = "Chuck norris can unit test entire appcs with a single assert"

        when: "addPost is invoked"
        controller.addPost()

        then: "Our flash message and redirect confirms the success"
        flash.message == "Successfully created post"
        response.redirectedUrl == "/post/timeline/${chuck.loginId}"
        Post.countByUser(chuck) == 1
    }

    @Unroll
    def "Index with id of #suppliedId redirects to #expectedUrl"() {
        given: "An id is supplied"
        params.id = suppliedId

        when: "Index is invoked"
        controller.index()

        then: "we are redirected to the expected url"
        response.redirectedUrl == expectedUrl

        where:
        suppliedId | expectedUrl
        'joe_cool' | '/post/timeline/joe_cool'
        null       | '/post/timeline/chuck_norris'
    }
}
