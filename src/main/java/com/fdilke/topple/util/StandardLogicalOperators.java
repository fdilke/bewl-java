package com.fdilke.topple.util;

import com.fdilke.topple.BaseTopos;
import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.Lambda;
import com.fdilke.topple.sugar.Quantifiers;
import com.fdilke.topple.sugar.Topos;
import com.fdilke.topple.sugar.dots.DotBiProduct;
import com.fdilke.topple.sugar.dots.DotProduct;
import com.fdilke.topple.sugar.dots.DotTruth;
import com.fdilke.topple.sugar.schema.BiProduct;
import com.fdilke.topple.sugar.schema.Exponential;
import com.fdilke.topple.sugar.schema.Product;
import com.fdilke.topple.sugar.schema.Truth;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 24-Feb-2008
 * Time: 20:40:51
 * To change this template use File | Settings | File Templates.
 */
public class StandardLogicalOperators<
	DOT extends ToposDot<DOT, ARROW>, 
	ARROW extends ToposArrow<DOT, ARROW>
> {
    private final Arrow<Product, Truth> and;
    private final Arrow<Product, Truth> or;
    private final Arrow<Product, Truth> implies;
    private final Arrow<Product, Truth> trueArrow;
    private final Arrow<Product, Truth> falseArrow;

    public StandardLogicalOperators(Topos<DOT, ARROW> topos) {
    	trueArrow = topos.trueArrow();
    	final DotTruth omega = topos.dotTruth();
    	final DotProduct omegaSquared = topos.product(BaseTopos.<Dot<?>>pair(omega, omega));
  
        Arrow<Product, Product> truthSquared = omegaSquared.multiply(
        		topos.dotUnitAsProduct(),
        		BaseTopos.<Arrow<Product, ?>>pair(trueArrow, trueArrow));
        and = omega.characteristic(truthSquared);

        Arrow<Truth, Truth> omegaIdentity = omega.getIdentity();
        Arrow<Truth, Product> diagonal = omegaSquared.multiply(omega,
        		BaseTopos.<Arrow<Truth, ?>>pair(omegaIdentity, omegaIdentity));
        final Arrow<Product, Truth> equals = omega.characteristic(diagonal);
        
        // a implies b if a ^ b == a
        implies = topos.arrow(omegaSquared, omega,
        		new Lambda<Product, Truth>() {
            public Truth lambda(Product product) {
            	Truth a = product.component(0, omega);
            	Truth aAndb = and.apply(product);
            	Product aAndb_a = omegaSquared.tuple(BaseTopos.<Element<?>>pair(aAndb, a));
            	return equals.apply(aAndb_a);
            }
        });
        
        Quantifiers<Truth> quantifiers = topos.quantifiers(omega);
        Arrow<Exponential<Truth, Truth>, Truth> forAllOmega = quantifiers.forAll();
        falseArrow = forAllOmega.compose(quantifiers.power().name(omegaIdentity))
        		.compose(topos.productToUnit());
                
        // Now a v b = for all w: ((a=>w) ^ (b=>w)) => w
        // Construct this by transposing the map ((a, b), w) => above formula, then composing with the quantifier
        
        DotBiProduct<Product, Truth> omegaCubed = topos.product(omegaSquared, omega);
        Arrow<BiProduct<Product, Truth>, Truth> map_w = omegaCubed.rightProjection();
        Arrow<BiProduct<Product, Truth>, Product> map_a_w = 
        	omegaSquared.multiply(omegaCubed, 
        		BaseTopos.<Arrow<BiProduct<Product, Truth>, ?>>pair(
        				omegaSquared.projection(0, omega)
        				.compose(omegaCubed.leftProjection()),
        				map_w));
        Arrow<BiProduct<Product, Truth>, Product> map_b_w = 
        	omegaSquared.multiply(omegaCubed, 
        		BaseTopos.<Arrow<BiProduct<Product, Truth>, ?>>pair(
        				omegaSquared.projection(1, omega).compose(
        						omegaCubed.leftProjection()),
        				map_w));
        Arrow<BiProduct<Product, Truth>, Truth> map_subformula = and.compose(
        		omegaSquared.multiply(omegaCubed,
        				BaseTopos.<Arrow<BiProduct<Product, Truth>, ?>>pair(
        						implies.compose(map_a_w),
        						implies.compose(map_b_w)
        		)));
        Arrow<BiProduct<Product, Truth>, Truth> map_formula = 
        	implies.compose(omegaSquared.multiply(
        		omegaCubed,
        		BaseTopos.<Arrow<BiProduct<Product, Truth>, ?>>pair(
        				map_subformula,
        				map_w
        		)));
        or = forAllOmega.compose(quantifiers.power().transpose(map_formula));       
     }
    
    public Arrow<Product, Truth> getAnd() {
        return and;
    }
    
    public Arrow<Product, Truth> getOr() {
        return or;
    }

    public Arrow<Product, Truth> getImplies() {
        return implies;
    }

    public Arrow<Product, Truth> getTrue() {
        return trueArrow; 
    }

    public Arrow<Product, Truth> getFalse() {
        return falseArrow; 
    }
}
