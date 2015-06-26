package com.fdilke.topple.sugar.algebra.universal;

import static com.fdilke.topple.fsets.FiniteSetsUtilities.compileArrow;
import static com.fdilke.topple.fsets.FiniteSetsUtilities.compileBiArrow;
import static com.fdilke.topple.fsets.FiniteSetsUtilities.compileDot;
import static com.fdilke.topple.fsets.FiniteSetsUtilities.constantArrow;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fdilke.topple.BaseTopos;
import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.fsets.FiniteSet;
import com.fdilke.topple.fsets.FiniteSetArrow;
import com.fdilke.topple.fsets.FiniteSets;
import com.fdilke.topple.fsets.FiniteSetsUtilities;
import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.Topos;
import com.fdilke.topple.sugar.schema.Product;

public class AlgebraBuilderTest { 
    private final Topos<FiniteSet, FiniteSetArrow> topos = new FiniteSets();
    private final FiniteSet letterSet = compileDot("a b c");
    private final Dot<Letter> dotLetter = topos.dot(letterSet, Letter.class);
    private final FiniteSet vectorSet = compileDot("T F");
    private final Dot<Vector> dotVector = topos.dot(vectorSet, Vector.class);
    private final Dot<Product> dotSquare = topos.product(BaseTopos.<Dot<?>>pair(dotLetter, dotLetter));
    private final Dot<Product> dotLetterVector = topos.product(BaseTopos.<Dot<?>>pair(dotLetter, dotVector));
	private final ProductDiagram<FiniteSet, FiniteSetArrow> square = topos.square(letterSet);
	private final ProductDiagram<FiniteSet, FiniteSetArrow> letterByVector = topos.product(letterSet, vectorSet);
    private final FiniteSetArrow pointA = constantArrow(topos, letterSet, "a");

	@Test(expected=IllegalArgumentException.class)
	public void axiomSchemeMustBeTagged() {
		@SuppressWarnings("unchecked") Class<? extends UntaggedAxiomScheme<Letter>>
			axiomClass = (Class<? extends UntaggedAxiomScheme<Letter>>) (Object) UntaggedAxiomScheme.class;
	
			AlgebraBuilder.build(topos, dotLetter, axiomClass,
					Collections.<String, Arrow<Product, Letter>>emptyMap()
			);
	}
	
	@Test
	public void emptyAxiomSchemeOk() {
		@SuppressWarnings("unchecked") Class<? extends EmptyAxiomScheme<Letter>>
			axiomClass = (Class<? extends EmptyAxiomScheme<Letter>>) (Object) EmptyAxiomScheme.class;
	
			AlgebraBuilder.build(topos, dotLetter, axiomClass,  
					Collections.<String, Arrow<Product, Letter>>emptyMap()
			);
	}

	@Test(expected=IllegalArgumentException.class)
	public void surplusOpsNotTolerated() {
		@SuppressWarnings("unchecked") Class<? extends EmptyAxiomScheme<Letter>>
			axiomClass = (Class<? extends EmptyAxiomScheme<Letter>>) (Object) EmptyAxiomScheme.class;
	
			AlgebraBuilder.build(topos, dotLetter, axiomClass, 
				Collections.<String, Arrow<Product, Letter>>singletonMap(
						"irrelevantOp", topos.nullaryOp(pointA, dotLetter)
				));
	}

	// Failing to provide the point in a pointed object
	@Test(expected=IllegalArgumentException.class)
	public void opsMissing() {
		@SuppressWarnings("unchecked") Class<? extends Pointed<Letter>>
			axiomClass = (Class<? extends Pointed<Letter>>) (Object) Pointed.class;
	
			AlgebraBuilder.build(topos, dotLetter, axiomClass,  
					Collections.<String, Arrow<Product, Letter>>emptyMap()
				);
	}

	// Misnaming the point operator
	@Test(expected=IllegalArgumentException.class)
	public void misnamedOp() {
		@SuppressWarnings("unchecked") Class<? extends Pointed<Letter>>
			axiomClass = (Class<? extends Pointed<Letter>>) (Object) Pointed.class;
	
			AlgebraBuilder.build(topos, dotLetter, axiomClass,
					Collections.<String, Arrow<Product, Letter>>singletonMap(
							"oddOpName", topos.nullaryOp(pointA, dotLetter)
			));
	}
	
	// Providing a unary point, when it should be nullary
	@Test(expected=IllegalArgumentException.class)
	public void wrongArityOp() {
		@SuppressWarnings("unchecked") Class<? extends Pointed<Letter>>
			axiomClass = (Class<? extends Pointed<Letter>>) (Object) Pointed.class;
	
			AlgebraBuilder.build(topos, dotLetter, axiomClass, 
					Collections.<String, Arrow<Product, Letter>>singletonMap(
							"point", topos.unaryOp(letterSet.getIdentity(), dotLetter)
			));
	}
	
	@Test
    public void lawlessAlgebra() {
		// A nonassociative multiplication: aa=b, all other products a; then (ab)b = ab = a, a(bb) = aa = b		 
	    MultiArrow<FiniteSet, FiniteSetArrow> naMultiply = 
	    	compileBiArrow(square, letterSet, letterSet, letterSet, 
	        		"a:a:b a:b:a a:c:a " +
	                "b:a:a b:b:a b:c:a " +
	                "c:a:a c:b:a c:c:a");

		Map<String, Arrow<Product, Letter>> opMap = new HashMap<String, Arrow<Product, Letter>>();
		opMap.put("point", topos.nullaryOp(pointA, dotLetter));
		opMap.put("multiply", topos.multiArrow(naMultiply, dotSquare, dotLetter));
			
		@SuppressWarnings("unchecked") Class<? extends PointedMagma<Letter>>
			axiomsClass = (Class<? extends PointedMagma<Letter>>) (Object) PointedMagma.class;

		PointedMagma<Letter> algebra = AlgebraBuilder.build(topos, dotLetter, axiomsClass, opMap);
		
		Letter a = constantElement(dotLetter, "a");
		Letter b = constantElement(dotLetter, "b");

		assertEquals(a, algebra.point());
		assertEquals(a, algebra.multiply(a, b));
		assertEquals(b, algebra.multiply(a, a));
	}

	// A simple algebra with a law: a pointed set with endomorphism fixing the point: Try with one that doesn't
	@Test(expected=IllegalArgumentException.class)
    public void badPointed1Set() {
		FiniteSetArrow badEndomap = compileArrow(letterSet, letterSet, "a:b b:c c:a");
		Map<String, Arrow<Product, Letter>> opMap = new HashMap<String, Arrow<Product, Letter>>();
		opMap.put("point", topos.nullaryOp(pointA, dotLetter));
		opMap.put("endomap", topos.unaryOp(badEndomap, dotLetter));
			
		@SuppressWarnings("unchecked") Class<? extends Pointed1Set<Letter>>
		axiomsClass = (Class<? extends Pointed1Set<Letter>>) (Object) Pointed1Set.class;

		AlgebraBuilder.build(topos, dotLetter, axiomsClass, opMap);
	}

	@Test
    public void pointed1Set() {
		FiniteSetArrow endomap = FiniteSetsUtilities.compileArrow(letterSet, letterSet, "a:a b:c c:a");
		Map<String, Arrow<Product, Letter>> opMap = new HashMap<String, Arrow<Product, Letter>>();
		opMap.put("point", topos.nullaryOp(pointA, dotLetter));
		opMap.put("endomap", topos.unaryOp(endomap, dotLetter));
			
		@SuppressWarnings("unchecked") Class<? extends Pointed1Set<Letter>>
			axiomsClass = (Class<? extends Pointed1Set<Letter>>) (Object) Pointed1Set.class;

		Pointed1Set<Letter> algebra = AlgebraBuilder.build(topos, dotLetter, axiomsClass, opMap);
		
		Letter a = constantElement(dotLetter, "a");
		Letter b = constantElement(dotLetter, "b");
		Letter c = constantElement(dotLetter, "c");

		assertEquals(a, algebra.point());
		assertEquals(a, algebra.endomap(a));
		assertEquals(c, algebra.endomap(b));
		assertEquals(a, algebra.endomap(c));
	}

	@Test
    public void commutativeMagma() {
	    MultiArrow<FiniteSet, FiniteSetArrow> commutativeNaMultiply = 
	    	compileBiArrow(square, letterSet, letterSet, letterSet, 
	        		"a:a:b a:b:a a:c:a " +
	                "b:a:a b:b:a b:c:a " +
	                "c:a:a c:b:a c:c:a");
		Map<String, Arrow<Product, Letter>> opMap = Collections.<String, Arrow<Product, Letter>>
			singletonMap("multiply", topos.multiArrow(commutativeNaMultiply, dotSquare, dotLetter));
			
		@SuppressWarnings("unchecked") Class<? extends CommutativeMagma<Letter>>
			axiomsClass = (Class<? extends CommutativeMagma<Letter>>) (Object) CommutativeMagma.class;

		CommutativeMagma<Letter> algebra = AlgebraBuilder.build(topos, dotLetter, axiomsClass, opMap);
		
		Letter a = constantElement(dotLetter, "a");
		Letter b = constantElement(dotLetter, "b");
		assertEquals(a, algebra.multiply(a, b));
		assertEquals(b, algebra.multiply(a, a));
	}

	@Test
    public void optionallyCommutativeMagmaThatIs() {
	    MultiArrow<FiniteSet, FiniteSetArrow> commutativeNaMultiply = 
	    	compileBiArrow(square, letterSet, letterSet, letterSet, 
	        		"a:a:b a:b:a a:c:a " +
	                "b:a:a b:b:a b:c:a " +
	                "c:a:a c:b:a c:c:a");
		Map<String, Arrow<Product, Letter>> opMap = Collections.<String, Arrow<Product, Letter>>
			singletonMap("multiply", topos.multiArrow(commutativeNaMultiply, dotSquare, dotLetter));
			
		@SuppressWarnings("unchecked") Class<? extends OptionallyCommutativeMagma<Letter>>
			axiomsClass = (Class<? extends OptionallyCommutativeMagma<Letter>>) (Object) OptionallyCommutativeMagma.class;

		OptionallyCommutativeMagma<Letter> algebra = AlgebraBuilder.build(topos, dotLetter, axiomsClass, opMap);
		
		Letter a = constantElement(dotLetter, "a");
		Letter b = constantElement(dotLetter, "b");
		assertEquals(a, algebra.multiply(a, b));
		assertEquals(b, algebra.multiply(a, a));
		assertTrue(algebra.checkOptionalLaw("commutative"));
	}

	@Test
    public void optionallyCommutativeMagmaThatIsNot() {
	    MultiArrow<FiniteSet, FiniteSetArrow> commutativeNaMultiply = 
	    	compileBiArrow(square, letterSet, letterSet, letterSet, 
	        		"a:a:b a:b:a a:c:a " +
	                "b:a:b b:b:a b:c:a " +
	                "c:a:a c:b:a c:c:a");
		Map<String, Arrow<Product, Letter>> opMap = Collections.<String, Arrow<Product, Letter>>
			singletonMap("multiply", topos.multiArrow(commutativeNaMultiply, dotSquare, dotLetter));
			
		@SuppressWarnings("unchecked") Class<? extends OptionallyCommutativeMagma<Letter>>
			axiomsClass = (Class<? extends OptionallyCommutativeMagma<Letter>>) (Object) OptionallyCommutativeMagma.class;

		OptionallyCommutativeMagma<Letter> algebra = AlgebraBuilder.build(topos, dotLetter, axiomsClass, opMap);
		
		Letter a = constantElement(dotLetter, "a");
		Letter b = constantElement(dotLetter, "b");
		assertEquals(a, algebra.multiply(a, b));
		assertEquals(b, algebra.multiply(a, a));
		assertFalse(algebra.checkOptionalLaw("commutative"));
	}
	
	@Test(expected=IllegalArgumentException.class)
    public void badCommutativeMagma() {
	    MultiArrow<FiniteSet, FiniteSetArrow> commutativeNaMultiply = 
	    	compileBiArrow(square, letterSet, letterSet, letterSet, 
	        		"a:a:b a:b:a a:c:a " +
	                "b:a:a b:b:a b:c:a " +
	                "c:a:b c:b:a c:c:a");
		Map<String, Arrow<Product, Letter>> opMap = Collections.<String, Arrow<Product, Letter>>
			singletonMap("multiply", topos.multiArrow(commutativeNaMultiply, dotSquare, dotLetter));
			
		@SuppressWarnings("unchecked") Class<? extends CommutativeMagma<Letter>>
			axiomsClass = (Class<? extends CommutativeMagma<Letter>>) (Object) CommutativeMagma.class;

		AlgebraBuilder.build(topos, dotLetter, axiomsClass, opMap);
	}
	
	// Test a structure that uses parameter spaces: the "left monod action"
	@Test public void parameterSpaces() {
		// We'll have a project {T, F} onto T, b project it onto F; so ab=a, ba=b; and c the unofficial unit element
	    MultiArrow<FiniteSet, FiniteSetArrow> associativeMultiply = 
	    	compileBiArrow(square, letterSet, letterSet, letterSet, 
	        		"a:a:a a:b:a a:c:a " +
	                "b:a:b b:b:b b:c:b " +
	                "c:a:a c:b:b c:c:c"); 
	    Map<String, Arrow<Product, Letter>> opMap = Collections.<String, Arrow<Product, Letter>>
	    	singletonMap("multiply", topos.multiArrow(associativeMultiply, dotSquare, dotLetter));
	    
	    @SuppressWarnings("unchecked") Class<? extends Monod<Letter>>
		monodClass = (Class<? extends Monod<Letter>>) (Object) Monod.class;
	    Monod<Letter> monod = AlgebraBuilder.build(topos, dotLetter, monodClass, opMap);
	    
	    MultiArrow<FiniteSet, FiniteSetArrow> scalarMultiply = 
	    	compileBiArrow(letterByVector, letterSet, vectorSet, vectorSet, 
	        		"a:T:T a:F:T " +
	                "b:T:F b:F:F " +
	                "c:T:T c:F:F");
	    
	    Map<String, Arrow<Product, Vector>> opMapAction = Collections.<String, Arrow<Product, Vector>>
	    	singletonMap("scalarMultiply", topos.multiArrow(scalarMultiply, dotLetterVector, dotVector));

	    Map<String, Algebra<?>> paramMap = Collections.<String, Algebra<?>> singletonMap("monod", monod);
	    
	    @SuppressWarnings("unchecked") Class<? extends LeftMonodAction<Letter, Vector>>
		actionClass = (Class<? extends LeftMonodAction<Letter, Vector>>) (Object) LeftMonodAction.class;
	    LeftMonodAction<Letter, Vector> action = AlgebraBuilder.build(topos, dotVector, actionClass, opMapAction, paramMap);
	    
	    assertTrue(monod == action.monod());
	    
		Letter a = constantElement(dotLetter, "a");
		Vector T = constantElement(dotVector, "T");
		assertEquals(T, action.scalarMultiply(a, T));
	}
	
	private <X extends Element<X>> X constantElement(Dot<X> dotX, String elementName) {
		FiniteSetArrow aArrow = constantArrow(topos, topos.unwrap(dotX), elementName);
		return topos.arrow(aArrow, topos.dotUnit(), dotX).asElement();
	}
	
	// An axiom scheme not tagged @Axioms
	public static abstract class UntaggedAxiomScheme<T> implements Algebra<UntaggedAxiomScheme<T>> {
	}

	// A valid, but completely empty axiom scheme
	@Axioms
	public static abstract class EmptyAxiomScheme<T> implements Algebra<EmptyAxiomScheme<T>> {
	}

	// Axioms for pointed objects
	@Axioms
	public static abstract class Pointed<T> implements Algebra<Pointed<T>> {
		@Operator public abstract T point();
	}
	
	// An "lawless" algebraic structure with only a distinguished element and a
	// nonassociative operation - a pointed magma-without-a-1
	@Axioms
	public static abstract class PointedMagma<T> implements Algebra<PointedMagma<T>> {
		@Operator public abstract T point();
		@Operator public abstract T multiply(T x, T y);		
	}

	// Axioms for pointed objects. These also test that one axiom scheme can extend another
	@Axioms
	public static abstract class PointedSet<T> implements Algebra<PointedSet<T>> {
		@Operator public abstract T point();
	}
	
	@Axioms
	public static abstract class Pointed1Set<T> extends PointedSet<T> {
		@Operator public abstract T endomap(T x);
		
		@Law public boolean endomapPreservesPoint() {
			return 
				point()				.equals( 
				endomap(point())
			);
		}
	}

	// A 'magma' - a set with a binary operation, subject to no laws
	@Axioms
	public static abstract class Magma<T> implements Algebra<Magma<T>> {
		@Operator public abstract T multiply(T x, T y);		
	}
	
	// Axioms for a set with a commutative binary operation
	@Axioms
	public static abstract class CommutativeMagma<T> extends Magma<T> {
		@Law public boolean commutative(T x, T y) {
			return equality(
				multiply(x, y),        
				multiply(y, x)
			);
		}
	}

	// Axioms for a set with an optionally commutative binary operation
	@Axioms
	public static abstract class OptionallyCommutativeMagma<T> extends Magma<T> {
		@OptionalLaw public boolean commutative(T x, T y) {
			return equality(
				multiply(x, y), 
				multiply(y, x)
			);
		}
	}
	
	// Axioms for a set with a associative binary operation
	@Axioms
	public static abstract class Monod<T> extends Magma<T> {
		@Law public boolean associative(T x, T y, T z) {
			return equality(
				multiply(x, multiply(y, z)), 
				multiply(multiply(x, y), z)
			);
		}
	}
	
	// Axioms for a "left monod action", to test the concept of parameter spaces
	@Axioms
	public static abstract class LeftMonodAction<X, T> implements Algebra<LeftMonodAction<X, T>> {
		@ParameterSpace public abstract Monod<X> monod();

		@Operator public abstract T scalarMultiply(@Parameter("monod") X x, T t);		

		@Law public boolean scalarAssociative(@Parameter("monod") X x, @Parameter("monod") X y, T t) {
			return equality(
				scalarMultiply(x, scalarMultiply(y, t)), 
				scalarMultiply(monod().multiply(x, y), t)
			);
		}
	}

	// Placeholder element class for most of our algebraic structures
    public static class Letter extends Element<Letter> {	
    }

	// Placeholder element class for left actions over a monod
    public static class Vector extends Element<Vector> {	
    }
}

