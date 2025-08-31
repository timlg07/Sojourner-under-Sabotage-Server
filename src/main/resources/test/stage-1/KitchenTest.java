import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/* For assertions you can use:
 - assertTrue
 - assertFalse
 - assertEquals
 - assertNotEquals
 - assertArrayEquals
 - assertThrows
 - assertNull
 - assertNotNull
 */

public class KitchenTest {

    @Test
    public void testAddIngredient() {
        Kitchen kitchen = new Kitchen();
        kitchen.addIngredient("flour", 100);
        assertEquals(100, kitchen.getIngredientAmount("flour"));
    }

    @Test
    public void testRemoveIngredient() {
        Kitchen kitchen = new Kitchen();
        kitchen.addIngredient("flour", 100);
        kitchen.removeIngredient("flour", 99);
        assertEquals(1, kitchen.getIngredientAmount("flour"));
    }

    @Test
    public void testRemoveIngredientNotEnough() {
        Kitchen kitchen = new Kitchen();
        kitchen.addIngredient("flour", 100);
        assertThrows(
                "Not enough flour available.",
                IllegalArgumentException.class,
                () -> {
                    kitchen.removeIngredient("flour", 101);
                }
        );
    }

    @Test
    public void testCookRecipe() {
        Kitchen kitchen = new Kitchen();
        kitchen.addIngredient("flour", 200);
        kitchen.addIngredient("sugar", 100);
        kitchen.addIngredient("butter", 200);
        kitchen.addIngredient("egg", 2);

        Map<String, Integer> recipe = Map.of(
            "flour", 200,
            "sugar", 80,
            "butter", 100,
            "egg", 2
        );

        kitchen.cookRecipe(recipe);

        assertEquals(0, kitchen.getIngredientAmount("egg"));
        assertEquals(0, kitchen.getIngredientAmount("flour"));
        assertEquals(20, kitchen.getIngredientAmount("sugar"));
        assertEquals(100, kitchen.getIngredientAmount("butter"));
    }

    @Test
    public void testCookRecipeNotEnoughIngredients() {
        Kitchen kitchen = new Kitchen();
        kitchen.addIngredient("tofu", 400);
        kitchen.addIngredient("soy sauce", 50);
        kitchen.addIngredient("rice", 10);
        kitchen.addIngredient("onion", 1);

        Map<String, Integer> recipe = Map.of(
            "tofu", 400,
            "soy sauce", 20,
            "rice", 200,
            "onion", 1
        );

        // If you're hungry now, here's the full recipe:
        // https://thewoksoflife.com/teriyaki-tofu
        // Be sure to get enough rice tho

        assertThrows(
                "Not enough rice available.",
                IllegalArgumentException.class,
                () -> {
                    kitchen.cookRecipe(recipe);
                }
        );
    }

    @Test
    public void testCookFailRemovesNoIngredients() {
        Kitchen kitchen = new Kitchen();
        kitchen.addIngredient("tofu", 400);
        kitchen.addIngredient("soy sauce", 50);
        kitchen.addIngredient("rice", 10);
        kitchen.addIngredient("onion", 1);

        Map<String, Integer> recipe = Map.of(
            "tofu", 400,
            "soy sauce", 20,
            "rice", 200,
            "onion", 1
        );

        // If you're hungry now, here's the full recipe:
        // https://thewoksoflife.com/teriyaki-tofu
        // Be sure to get enough rice tho

        try {
            kitchen.cookRecipe(recipe);
        } catch (IllegalArgumentException ignored) {}

        assertEquals(400, kitchen.getIngredientAmount("tofu"));
        assertEquals(50, kitchen.getIngredientAmount("soy sauce"));
        assertEquals(10, kitchen.getIngredientAmount("rice"));
        assertEquals(1, kitchen.getIngredientAmount("onion"));
    }

}
