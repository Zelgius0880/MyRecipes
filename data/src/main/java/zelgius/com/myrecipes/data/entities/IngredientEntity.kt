package zelgius.com.myrecipes.data.entities

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import zelgius.com.myrecipes.data.model.Ingredient
import zelgius.com.myrecipes.data.model.Recipe

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ref_ingredient"]
        ),
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["ref_recipe"]
        ),
        ForeignKey(entity = StepEntity::class, parentColumns = ["id"], childColumns = ["ref_step"])
    ],
    indices = [
        Index(value = ["ref_ingredient"]),
        Index(value = ["ref_recipe"]),
        Index(value = ["ref_step"])
    ]
)
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var quantity: Double,
    var unit: IngredientEntity.Unit,
    var optional: Boolean?,
    @ColumnInfo(name = "sort_order") var sortOrder: Int,
    @ColumnInfo(name = "ref_ingredient") var refIngredient: Long?,
    @ColumnInfo(name = "ref_recipe") var refRecipe: Long?,
    @ColumnInfo(name = "ref_step") var refStep: Long?
)

@Entity(tableName = "Ingredient")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true) var id: Long?,
    var name: String,
    @ColumnInfo(name = "image_url") var imageURL: String?

) {

    //constructor() : this (null, "", null)

    enum class Unit {
        MILLILITER,
        LITER,
        UNIT,
        TEASPOON,
        TABLESPOON,
        GRAMME,
        KILOGRAMME,
        CUP,
        PINCH
    }

    override fun toString(): String {
        return name
    }
}


fun IngredientEntity.asModel() = Ingredient(
    id = id,
    quantity = 0.0,
    unit = Ingredient.Unit.Unit,
    name = name,
    imageUrl = imageURL,
    optional = false,
    sortOrder = 0,
    recipe = null,
    step = null,
)


fun IngredientEntity.Unit.asModel(): Ingredient.Unit = when (this) {
    IngredientEntity.Unit.MILLILITER -> Ingredient.Unit.Milliliter
    IngredientEntity.Unit.LITER -> Ingredient.Unit.Liter
    IngredientEntity.Unit.UNIT -> Ingredient.Unit.Unit
    IngredientEntity.Unit.TEASPOON -> Ingredient.Unit.TeaSpoon
    IngredientEntity.Unit.TABLESPOON -> Ingredient.Unit.TableSpoon
    IngredientEntity.Unit.GRAMME -> Ingredient.Unit.Gramme
    IngredientEntity.Unit.KILOGRAMME -> Ingredient.Unit.Kilogramme
    IngredientEntity.Unit.CUP -> Ingredient.Unit.Cup
    IngredientEntity.Unit.PINCH -> Ingredient.Unit.Pinch
}