package com.fdilke.topple;

import static com.fdilke.topple.BaseTopos.pair;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.Lambda;
import com.fdilke.topple.sugar.Topos;
import com.fdilke.topple.sugar.algebra.schema.HeytingAlgebra;
import com.fdilke.topple.sugar.dots.DotBiProduct;
import com.fdilke.topple.sugar.dots.DotExponential;
import com.fdilke.topple.sugar.dots.DotProduct;
import com.fdilke.topple.sugar.schema.Exponential;
import com.fdilke.topple.sugar.schema.BiProduct;
import com.fdilke.topple.sugar.schema.Product;
import com.fdilke.topple.sugar.schema.Truth;
import com.fdilke.topple.sugar.schema.Unit;

public abstract class AbstractToposTest<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>> {
	private final ToposFixtures<DOT, ARROW> fixtures;
    private final Topos<DOT, ARROW> topos;
    
    public AbstractToposTest(ToposFixtures<DOT, ARROW> fixtures) {
    	this.fixtures = fixtures;
        topos = fixtures.getTopos();
    }

    // Make sure we can build the identity arrow
    @Test  
    public void buildIdentity() {
    	DOT src = fixtures.dotFoo();
       	Dot<Source> dotSrc = dot(src, Source.class);
        
        Arrow<Source, Source> identityArrow = arrow(dotSrc, dotSrc,
                new Lambda<Source, Source>() {
            public Source lambda(Source element) {
                return element;
            }
        });
        assertEquals(dotSrc.getIdentity(), identityArrow);
    }
    
    // Make sure we can reconstruct an arrow we already have
    @Test
    public void rebuildExistingArrow() {
    	DOT src = fixtures.dotFoo();
    	DOT tgt = fixtures.dotBar();
        ARROW arrow = fixtures.arrowFooToBar();

    	Dot<Source> dotSrc = dot(src, Source.class);
    	Dot<Target> dotTgt = dot(tgt, Target.class);
        final Arrow<Source, Target> wrappedArrow = arrow(arrow, dotSrc,  dotTgt);
        Arrow<Source, Target> rebuiltArrow = arrow(dotSrc, dotTgt,
                new Lambda<Source, Target>() {
            public Target lambda(Source element) {
                return wrappedArrow.apply(element);
            }
        });
        assertEquals(wrappedArrow, rebuiltArrow);
    }
    
    // Make sure we can compose arrows inside of a "wrapped closure"
    @Test
    public void composeArrows() {
        ARROW f = fixtures.arrowFooToBar();
        ARROW g = fixtures.arrowBarToBaz();
        DOT src = fixtures.dotFoo();
        DOT mid = fixtures.dotBar();
        DOT tgt = fixtures.dotBaz();
        
    	Dot<Source> dotSrc = dot(src, Source.class);
    	Dot<Mid>    dotMid = dot(mid, Mid.class);
    	Dot<Target> dotTgt = dot(tgt, Target.class);
    	final Arrow<Source, Mid> arrowF = arrow(f, dotSrc,  dotMid);
        final Arrow<Mid, Target> arrowG = arrow(g, dotMid, dotTgt);
        Arrow<Source, Target> composedArrow = arrow(dotSrc, dotTgt,
                new Lambda<Source, Target>() {
            public Target lambda(Source element) {
                return arrowG.apply(arrowF.apply(element));
            }
        });
        Arrow<Source, Target> expectedComposite = arrow(g.compose(f), dotSrc,  dotTgt);
        assertEquals(expectedComposite, composedArrow);
	}

    // basic operations with biproducts
    @Test public void biproducts() {
        ARROW src2target = fixtures.arrowFooToBar();
        ARROW src2target2 = fixtures.arrowFooToBaz();
        DOT src = fixtures.dotFoo();
        DOT tgt = fixtures.dotBar();
        DOT tgt2 = fixtures.dotBaz();
      
    	Dot<Source> dotSrc = dot(src, Source.class);
    	Dot<Target> dotTgt = dot(tgt, Target.class);
    	Dot<Target2> dotTgt2 = dot(tgt2, Target2.class);
        final DotBiProduct<Target, Target2> productDot = product(dotTgt, dotTgt2);
        final Arrow<Source, Target> arrowF = arrow(src2target, dotSrc,  dotTgt);
        final Arrow<Source, Target2> arrowG = arrow(src2target2, dotSrc, dotTgt2);
        // Test each projection of the product element
        Arrow<Source, Target> leftArrow = arrow(dotSrc, dotTgt,
                new Lambda<Source, Target>() {
            public Target lambda(Source element) {
                Target left = arrowF.apply(element);
                Target2 right = arrowG.apply(element);
                BiProduct<Target, Target2> product = productDot.pair(left, right);
                return product.left();
            }
        });
        assertEquals(arrowF, leftArrow);
        // ... and the other factor
        Arrow<Source, Target2> rightArrow = arrow(dotSrc, dotTgt2,
                new Lambda<Source, Target2>() {
            public Target2 lambda(Source element) {
                Target left = arrowF.apply(element);
                Target2 right = arrowG.apply(element);
                BiProduct<Target, Target2> product = productDot.pair(left, right);
                return product.right();
            }
        });
        assertEquals(arrowG, rightArrow);
    }
    
    // check properties of the "product dot" object, by constructing the canonical isomorphism and its
    // inverse between the wrapped product and our "unadorned" one
    @Test public void biproductDotIsomorphic() {
    	DOT mid = fixtures.dotFoo();
    	DOT tgt = fixtures.dotBar();

    	Dot<Mid>    dotMid = dot(mid, Mid.class);
    	Dot<Target> dotTgt = dot(tgt, Target.class);
  
    	final DotBiProduct<Mid, Target> productDot = product(dotMid, dotTgt);
        final ProductDiagram<DOT, ARROW> myProduct = topos.getProductDiagram(pair(mid, tgt));
        final Dot<MyProductElement> myProductDot = dot(myProduct.getProduct(), MyProductElement.class);
        // Construct an isomorphism from our "simple" product to the wrapped one
        
        ARROW projectLeft = myProduct.getProjections().get(0);
        ARROW projectRight = myProduct.getProjections().get(1);
        final Arrow<MyProductElement, Mid> projectLeftB = arrow(projectLeft, myProductDot,  dotMid);
        final Arrow<MyProductElement, Target> projectRightB = arrow(projectRight, myProductDot,  dotTgt);
        Arrow<MyProductElement, BiProduct<Mid, Target>> iso = arrow(myProductDot, productDot,
                new Lambda<MyProductElement, BiProduct<Mid, Target>>() {
            public BiProduct<Mid, Target> lambda(MyProductElement myProductElement) {
                Mid leftElement = projectLeftB.apply(myProductElement);
                Target rightElement = projectRightB.apply(myProductElement);
                return productDot.pair(leftElement, rightElement);
            }
        });
        // ... and its inverse
        Arrow<BiProduct<Mid, Target>, MyProductElement> isoInv = arrow(productDot, myProductDot,
                new Lambda<BiProduct<Mid, Target>, MyProductElement>() {
            public MyProductElement lambda(BiProduct<Mid, Target> productElement) {
                Mid leftElement = productElement.left();
                Target rightElement = productElement.right();
                ARROW leftArrow = unwrap(leftElement);
                ARROW rightArrow = unwrap(rightElement);
                List<ARROW> components = pair(leftArrow, rightArrow);
                ARROW productArrow = myProduct.multiplyArrows(leftArrow.getSource(), components);
                return element(myProductDot, productArrow);
            }
        });
        
        // make sure they're inverses
        assertEquals(productDot.getIdentity(), iso.compose(isoInv));
        assertEquals(myProductDot.getIdentity(), isoInv.compose(iso));
	}
    
    // equivalent operations with multiproducts
    @Test public void multiproducts() {
        ARROW src2target = fixtures.arrowFooToBar();
        ARROW src2target2 = fixtures.arrowFooToBaz();
        DOT src = fixtures.dotFoo();
        DOT tgt = fixtures.dotBar();
        DOT tgt2 = fixtures.dotBaz();
      
    	Dot<Source> dotSrc = dot(src, Source.class);
    	final Dot<Target> dotTgt = dot(tgt, Target.class);
    	final Dot<Target2> dotTgt2 = dot(tgt2, Target2.class);
        final DotProduct productDot = product(BaseTopos.<Dot<?>>pair(dotTgt, dotTgt2));
        final Arrow<Source, Target> arrowF = arrow(src2target, dotSrc,  dotTgt);
        final Arrow<Source, Target2> arrowG = arrow(src2target2, dotSrc, dotTgt2);
        // Test each projection of the product element
        Arrow<Source, Target> leftArrow = arrow(dotSrc, dotTgt,
                new Lambda<Source, Target>() {
            public Target lambda(Source element) {
                Target left = arrowF.apply(element);
                Target2 right = arrowG.apply(element);
                Product product = productDot.tuple(
                        (List<Element<?>>) pair(left, right)
                );
                return product.component(0, dotTgt);
            }
        });
        assertEquals(arrowF, leftArrow);
        // ... and the other factor
        Arrow<Source, Target2> rightArrow = arrow(dotSrc, dotTgt2,
                new Lambda<Source, Target2>() {
            public Target2 lambda(Source element) {
                Target left = arrowF.apply(element);
                Target2 right = arrowG.apply(element);
                Product product = productDot.tuple((List<Element<?>>)pair(left, right));
                return product.component(1, dotTgt2);
            }
        });
        assertEquals(arrowG, rightArrow);
    }

    @Test public void multiproductDotIsomorphic() {
    	DOT mid = fixtures.dotFoo();
    	DOT tgt = fixtures.dotBar();

    	final Dot<Mid>    dotMid = dot(mid, Mid.class);
    	final Dot<Target> dotTgt = dot(tgt, Target.class);
  
    	final DotProduct productDot = product(BaseTopos.<Dot<?>>pair(dotMid, dotTgt));
        final ProductDiagram<DOT, ARROW> myProduct = topos.getProductDiagram(pair(mid, tgt));
        final Dot<MyProductElement> myProductDot = dot(myProduct.getProduct(), MyProductElement.class);
        // Construct an isomorphism from our "simple" product to the wrapped one
        
        ARROW projectLeft = myProduct.getProjections().get(0);
        ARROW projectRight = myProduct.getProjections().get(1);
        final Arrow<MyProductElement, Mid> projectLeftB = arrow(projectLeft, myProductDot,  dotMid);
        final Arrow<MyProductElement, Target> projectRightB = arrow(projectRight, myProductDot,  dotTgt);
        Arrow<MyProductElement, Product> iso = arrow(myProductDot, productDot,
                new Lambda<MyProductElement, Product>() {
            public Product lambda(MyProductElement myProductElement) {
                Mid leftElement = projectLeftB.apply(myProductElement);
                Target rightElement = projectRightB.apply(myProductElement);
                return productDot.tuple(
                        (List<Element<?>>)pair(leftElement, rightElement)
                );
            }
        });
        // ... and its inverse
        Arrow<Product, MyProductElement> isoInv = arrow(productDot, myProductDot,
                new Lambda<Product, MyProductElement>() {
            public MyProductElement lambda(Product productElement) {
                Mid leftElement = productElement.component(0, dotMid);
                Target rightElement = productElement.component(1, dotTgt);
                ARROW leftArrow = unwrap(leftElement);
                ARROW rightArrow = unwrap(rightElement);
                List<ARROW> components = pair(leftArrow, rightArrow);
                ARROW productArrow = myProduct.multiplyArrows(leftArrow.getSource(), components);
                return element(myProductDot, productArrow);
            }
        });
        
        // make sure they're inverses
        assertEquals(productDot.getIdentity(), iso.compose(isoInv));
        assertEquals(myProductDot.getIdentity(), isoInv.compose(iso));
	}
    
    // Basic operations with exponentials - evaluation, transpose
    @Test
    public void exponentials() {
    	DOT foo = fixtures.dotFoo();
    	DOT bar = fixtures.dotBar();
    	DOT baz = fixtures.dotBaz();
    	
    	Dot<Foo> dotFoo = dot(foo, Foo.class);
    	Dot<Bar> dotBar = dot(bar, Bar.class);
    	Dot<Baz> dotBaz = dot(baz, Baz.class);
    	
    	Dot<BiProduct<Foo, Bar>> dotProduct = product(dotFoo, dotBar);
    	DotExponential<Bar, Baz> expDot = exponential(dotBar, dotBaz);
    	Arrow<BiProduct<Foo, Bar>, Baz> wrappedMultiArrow = topos.biArrow(fixtures.arrowFooBarToBaz(), dotProduct, dotBaz);
    	final Arrow<Foo, Exponential<Bar, Baz>> transpose = expDot.transpose(wrappedMultiArrow);
    	// Check the defining property of the transpose, that we can reconstruct the multiarrow from it
        Arrow<BiProduct<Foo, Bar>, Baz> altMultiArrow = arrow(dotProduct, dotBaz,
                new Lambda<BiProduct<Foo, Bar>, Baz>() {
            public Baz lambda(BiProduct<Foo, Bar> productElement) {
                Foo fooElement = productElement.left();
                Bar barElement = productElement.right();
                return transpose.apply(fooElement).evaluate(barElement);
            }
        });
    	assertEquals(wrappedMultiArrow, altMultiArrow);
    }

    // Make sure we can construct constant arrows to the terminator (represented by Unit)
    @Test
    public void constantArrows() {
		DOT src = fixtures.dotFoo();
		DOT tgt = fixtures.dotBar();
        ARROW f = fixtures.arrowFooToBar();
        
    	Dot<Source> dotSrc = dot(src, Source.class);
    	Dot<Target> dotTgt = dot(tgt, Target.class);

        Arrow<Source, Unit> constantArrowSource = constantArrow(dotSrc);
        Arrow<Target, Unit> constantArrowTarget = constantArrow(dotTgt);
        Arrow<Source, Target> arrowF = arrow(f, dotSrc,  dotTgt);
        Arrow<Source, Unit> constantArrow2 = constantArrowTarget.compose(arrowF);
        assertEquals(constantArrowSource, constantArrow2);
	}
    
    // kick the tyres of the Heyting algebra of truth values
    @Test public void testTruthValues() {
    	HeytingAlgebra<Truth> truthAlgebra = topos.truthAlgebra();
    	assertTrue(truthAlgebra.checkOptionalLaw("distributive"));
    	assertTrue(truthAlgebra.checkOptionalLaw("modular"));
    	assertEquals(truthAlgebra.FALSE(), truthAlgebra.not(truthAlgebra.TRUE()));
    	assertEquals(truthAlgebra.TRUE(), truthAlgebra.not(truthAlgebra.FALSE()));
    }
	
    // Delegate methods to facilitate building dots, arrows, structures
    public <E extends Element<E>, F extends Element<F>> Arrow<E, F>
    arrow(ARROW arrow, Dot<E> src, Dot<F> dest) {
        return topos.arrow(arrow, src, dest);
    }

	public <LEFT extends Element<LEFT>, RIGHT extends Element<RIGHT>> 
		DotBiProduct<LEFT, RIGHT> product(
			Dot<LEFT> dotLeft, Dot<RIGHT> dotRight) {
		return topos.product(dotLeft, dotRight);
	}

	public DotProduct product(List<Dot<?>> dots) {
		return topos.product(dots);
	}
	
	public <E extends Element<E>, F extends Element<F>> 
		DotExponential<E, F> exponential(
			Dot<E> dotE, Dot<F> dotF) {
		return topos.exponential(dotE, dotF);
	}

	public <E extends Element<E>> 
	Dot<E> dot(DOT dot,
		Class<E> classE) {
		return topos.dot(dot, classE);
	}
	
	public <E extends Element<E>, F extends Element<F>>
		Arrow<E, F> arrow(
			Dot<E> dotE, Dot<F> dotF,
			Lambda<E, F> lambda) {
		return topos.arrow(dotE, dotF, lambda);
	}	
	
	public <E extends Element<E>> DOT unwrap(Dot<E> dot) {
		return topos.unwrap(dot);
	}

	public <SRC extends Element<SRC>, TGT extends Element<TGT>> ARROW unwrap(Arrow<SRC, TGT> arrow) {
		return topos.unwrap(arrow);
	}
	
	public <E extends Element<E>> ARROW unwrap(E element) {
		return topos.unwrap(element);
	}
	
	public <E extends Element<E>> E element(Dot<E> dot, ARROW arrow) {
		return topos.element(dot, arrow);
	}
	
	public <E extends Element<E>> Arrow<E, Unit> constantArrow(Dot<E> dot) {
		return topos.constantArrow(dot);
	}

	// Tag classes representing virtual elements
    public static class Source extends Element<Source> {	
    }

    public static class Mid extends Element<Mid> {	
    }

    public static class Target extends Element<Target> {	
    }

    public static class Target2 extends Element<Target2> {	
    }

    public static class MyProductElement extends Element<MyProductElement> { 	
    }

    public static class MyExponentialElement extends Element<MyExponentialElement> { 	
    }

    public static class Foo extends Element<Foo> {	
    }
    
    public static class Bar extends Element<Bar> {	
    }
    
    public static class Baz extends Element<Baz> {	
    }
}
