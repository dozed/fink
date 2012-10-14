package fink.data

//import net.liftweb.json._
//import net.liftweb.json.Serialization.{read => jsread, write => jswrite}
//import net.liftweb.json.JsonDSL._

// class PostSerializer extends Serializer[Post] {
//   private val Class = classOf[Post]

//   def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Post] = {
//     // case (TypeInfo(Class, _), JObject(
//     //          JField("id", JInt(id))
//     //       :: JField("catId", JInt(catId))
//     //       :: JField("date", JInt(date))
//     //       :: JField("author", JString(author))
//     //       :: JField("title", JString(title))
//     //       :: JField("text", JString(text))
//     //       :: JField("tags", JArray(tags))
//     //       :: Nil)) =>
//     //   println("foo")
//     //   val p = Post(id.longValue, catId.longValue, date.longValue, title, author, text)
//     //   p

//     case (TypeInfo(Class, _), json) =>
//       val id = (json \ "id").extract[Long]
//       val catId = (json \ "catId").extract[Long]
//       val date = (json \ "date").extract[Long]
//       val author = (json \ "author").extract[String]
//       val text = (json \ "text").extract[String]
//       val title = (json \ "title").extract[String]

//       val p = Post(id, catId, date, title, author, text)
//       p
//   }

//   def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
//     case x: Post =>
//       ("id" -> x.id) ~
//       ("catId" -> x.catId) ~
//       ("date" -> x.date) ~
//       ("title" -> x.title) ~
//       ("author" -> x.author) ~
//       ("text" -> x.text) ~
//       ("tags" -> (x.tags.map { tag =>
//         ("id" -> tag.id) ~
//         ("name" -> tag.name)
//       }))
//   }
// }

// processes incoming JSON requests, create and update
// object RecipeProcessor {
//  def apply(x:Any) = x match {
//    case JObject(JField("id", JInt(id)) :: JField("title", JString(title)) :: JField("instructions", JString(instructions)) :: JField("ingredients", JArray(list)) :: Nil) =>
//      val recipe = Recipe(id.longValue, title, instructions)

//      // TODO persist only when needed, update otherwise
//      Library.recipes.insert(recipe)

//      list.foreach { el =>
//        el match {
//          case JObject(JField("id", JInt(id)) :: JField("amount", JString(amount)) :: JField("unit", JString(unit)) :: JField("description", JString(description)) :: Nil) =>
//            val s = Stuff(id.longValue, description)

//            // TODO persist stuff only if needed, update otherwise
//            id.longValue match {
//              case 0 => Library.stuff.insert(s)
//              case _ => // update name...
//            }

//            val i = Ingredient(recipe.id.longValue, s.id.longValue, amount, unit)
//            recipe.ingredients.associate(s, i)
//            i
//          case _ => throw new MappingException("Can't process " + x)
//        }
//      }

//      recipe
//    case x => throw new MappingException("Can't process " + x)
//  }
// }


