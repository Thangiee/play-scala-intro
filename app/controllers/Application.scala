package controllers

import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Redirect(routes.Products.list())
  }

  def hello(name: String) = Action {
    Ok(views.html.hello(name))
  }
}