package com.fdilke.topple.fsets;

import java.util.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.impl.FiniteSetsMultiplier;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 16-Feb-2008
 * Time: 10:23:24
 * To change this template use File | Settings | File Templates.
 */
public class FiniteSetsMultiplierTest {

    private static final FiniteSet _fruitSet = new FiniteSet(new HashSet<Object>(Arrays.asList(
            "apple", "orange", "banana"
    )));
    private static final FiniteSet _numeralSet = new FiniteSet(new HashSet<Object>(Arrays.asList(
            "I", "II"
    )));

    @Test
    public void emptyProduct() {
        List<FiniteSet> emptyList = Collections.emptyList();
        FiniteSet product = FiniteSetsMultiplier.multiply(emptyList);

        Set<Object> productSet = product.getUnderlyingSet();

        assertEquals(1, productSet.size());
        Object productElement = new ArrayList<Object>(productSet).get(0);
        assertTrue(productElement instanceof Object[]);
        Object[] productArray = (Object[]) productElement;
        assertEquals(0, productArray.length);
    }

    @Test
    public void unaryProduct() {
        List<FiniteSet> unaryList = Arrays.asList(_fruitSet);
        FiniteSet productFiniteSet = FiniteSetsMultiplier.multiply(unaryList);

        Set<Object> productSet = productFiniteSet.getUnderlyingSet();

        assertEquals(_fruitSet.getUnderlyingSet().size(), productSet.size());
        for (Object productElement : productSet) {
            assertTrue(productElement instanceof Object[]);
            Object[] productArray = (Object[]) productElement;
            assertEquals(1, productArray.length);
            Object product = productArray[0];
            assertTrue(_fruitSet.getUnderlyingSet().contains(product));
        }
    }

    @Test
    public void doubleProduct() {
        List<FiniteSet> doubleList = Arrays.asList(_fruitSet, _numeralSet);
        FiniteSet productFiniteSet = FiniteSetsMultiplier.multiply(doubleList);

        Set<Object> productSet = productFiniteSet.getUnderlyingSet();
        Set<String> productStringSet = new HashSet<String>();
        for (Object x : productSet) {
            Object[] array = (Object[]) x;
            productStringSet.add(Arrays.deepToString(array));
        }
        Set<Object> expectedProductStringSet = new HashSet<Object>(Arrays.asList(
                "[apple, I]",
                "[orange, I]",
                "[banana, I]",
                "[apple, II]",
                "[orange, II]",
                "[banana, II]"
        ));
        assertEquals(expectedProductStringSet, productStringSet);
    }
}

