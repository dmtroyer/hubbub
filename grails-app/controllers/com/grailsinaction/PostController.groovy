package com.grailsinaction

class PostController {

    static scaffold = Post

    def index() {
        if (!params.id) params.id = "chuck_norris"
        redirect(action: 'timeline', params: params)
    }

    def addPost() {
        def user = User.findByLoginId(params.id)
        if (user) {
            def post = new Post(params)
            user.addToPosts(post)
            if (user.save()) flash.message = "Successfully created post"
            else flash.message = "Invalid or empty post"
        } else {
            flash.message = "Invalid User Id"
        }
        redirect(action: 'timeline', id: params.id)
    }

    def timeline() {
        def user = User.findByLoginId(params.id)
        if (user) [ user: user ]
        else response.sendError(404)
    }

}
