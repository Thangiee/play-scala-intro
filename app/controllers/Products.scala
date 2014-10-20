package controllers

import model.Product
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.Messages
import play.api.mvc.{Action, Controller, Flash}

object Products extends Controller {

  val addForm: Form[Product] = Form(
    mapping(
      "ean" → longNumber.verifying(
        "validation.ean.duplicate", Product.findByEan(_).isEmpty),
      "name" → nonEmptyText,
      "description" → nonEmptyText
    )(Product.apply)(Product.unapply)
  )

  val editForm: Form[Product] = Form(
    mapping(
      "ean" → longNumber,
      "name" → nonEmptyText,
      "description" → nonEmptyText
    )(Product.apply)(Product.unapply)
  )

  def list = Action { implicit request =>
    val products = Product.findAll

    Ok(views.html.products.list(products))
  }

  def show(ean: Long) = Action { implicit request =>
    Product.findByEan(ean).map { product =>
      Ok(views.html.products.details(product))
    }.getOrElse(NotFound)
  }

  def newProduct = Action { implicit request =>
    val form = if (request.flash.get("error").isDefined)
      addForm.bind(request.flash.data)
    else
      addForm
    Ok(views.html.products.addProduct(form))
  }

  def editProduct(ean: Long) = Action { implicit request =>
    Product.findByEan(ean).map { product =>
      val form = addForm.bindFromRequest().fill(product)
      Ok(views.html.products.editProduct(form, product))
    }.getOrElse(NotFound)
  }

  def saveEdit(ean: Long) = Action { implicit request =>
    val newProductForm = editForm.bindFromRequest()

    newProductForm.fold(
      hasErrors = { form =>
        Redirect(routes.Products.editProduct(ean)). // <---
          flashing(Flash(form.data) + ("error" → "wong idiot"))
      },
      success = { newProduct =>
        Product.edit(newProduct)
        Redirect(routes.Products.show(newProduct.ean)).
          flashing("success" → "Saved")
      }
    )
  }

  def saveAdd = Action { implicit request =>
    val newProductForm = addForm.bindFromRequest()

    newProductForm.fold(
      hasErrors = { form =>
        Redirect(routes.Products.newProduct()). // <---
          flashing(Flash(form.data) + ("error" → "wong idiot"))
      },
      success = { newProduct =>
        Product.add(newProduct)
        val message = Messages("products.new.success", newProduct.name)
        Redirect(routes.Products.show(newProduct.ean)).
          flashing("success" → message)
      }
    )
  }
}
