import java.util.HashMap;
import java.util.Map;

public class Kitchen {

    private final Map<String, Integer> ingredients;

    public Kitchen() {
        ingredients = new HashMap<>();
    }

    public int getIngredientAmount(String ingredient) {
        return ingredients.getOrDefault(ingredient, 0);
    }

    public void addIngredient(String ingredient, int amount) {
        int current = getIngredientAmount(ingredient);
        ingredients.put(ingredient, current + amount);
    }

    public void removeIngredient(String ingredient, int amount) {
        int current = getIngredientAmount(ingredient);
        if (current < amount) {
            throw new IllegalArgumentException("Not enough " + ingredient + " available.");
        }
        ingredients.put(ingredient, current - amount);
    }

    public void cookRecipe(Map<String, Integer> recipe) {
        boolean success = true;
        for (Map.Entry<String, Integer> entry : recipe.entrySet()) {
            // first check if the ingredient is available
            String ingredient = entry.getKey();
            int requiredAmount = entry.getValue();
            int availableAmount = getIngredientAmount(ingredient);
            if (availableAmount < requiredAmount) {
                success = false;
            } else {
                // use the ingredient
                removeIngredient(ingredient, requiredAmount);
            }
        }

        if (!success) {
            throw new IllegalArgumentException("Some ingredients were missing.");
        }
    }
}
