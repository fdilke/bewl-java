package com.fdilke.topple.sugar.algebra.universal;

import static com.fdilke.topple.BaseTopos.pair;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.MethodHandler;

import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.sugar.Arrow;
import com.fdilke.topple.sugar.Dot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.sugar.Topos;
import com.fdilke.topple.sugar.schema.Product;

public class AlgebraBuilder<
	DOT extends ToposDot<DOT, ARROW>, 
	ARROW extends ToposArrow<DOT, ARROW>,
	AXIOMS extends Algebra<? super AXIOMS>,
	ELEMENT extends Element<ELEMENT>
> {
	private static final Map<String, Method> algebraMethods = findAlgebraMethods(); 
 	private final Topos<DOT, ARROW> topos;
	private final Dot<ELEMENT> dotElement;
	private final Class<? extends AXIOMS> axiomsClass;
	private final ParsedAxioms<DOT, ARROW, AXIOMS> axioms;
	private final Map<String, MultiArrow<DOT, ARROW>> opMap;
	private final Map<String, Algebra<?>> pSpaceMap;
	private final Map<String, Boolean> verifiedOptionalLaws = new HashMap<String, Boolean>();
	private AXIOMS algebra;

	private AlgebraBuilder(Topos<DOT, ARROW> topos, 
			Dot<ELEMENT> dotElement,
			Class<? extends AXIOMS> axiomsClass,
			Map<String, Arrow<Product, ELEMENT>> opMap,
			Map<String, Algebra<?>> pSpaceMap) {
		this.topos = topos;
		this.dotElement = dotElement;
		this.axiomsClass = axiomsClass;
		this.axioms = ParsedAxioms.<DOT, ARROW, AXIOMS>parse(axiomsClass, pSpaceMap);
		this.opMap = unwrapOperators(opMap);
		this.pSpaceMap = pSpaceMap;
	}
	
	private Map<String, MultiArrow<DOT, ARROW>> unwrapOperators(
			Map<String, Arrow<Product, ELEMENT>> opMap) {
		Map<String, MultiArrow<DOT, ARROW>> newOpMap = new HashMap<String, MultiArrow<DOT, ARROW>>(); 
		for (String opName : opMap.keySet()) {
			Arrow<Product, ELEMENT> op = opMap.get(opName);
			newOpMap.put(opName, topos.unwrapMultiArrow(op));
		}
		return newOpMap;
	}

	private static Map<String, Method> findAlgebraMethods() {
		Map<String, Method> methods = new HashMap<String, Method>();
		for(Method method : Algebra.class.getDeclaredMethods()) {
			methods.put(method.getName(), method);
		}
		return methods;
	}
	
	private void verifyStrictLaws() {
		for (ParsedLaw<DOT, ARROW, AXIOMS> law : axioms.getLaws().values()) {
			if (law.isStrict() && !verifyLaw(law)) {
					throw new IllegalArgumentException("Law " + law.getName() 
							+ " verification failed in " + axiomsClass.getName());
			}
		}
	}
	
	private boolean verifyLaw(ParsedLaw<DOT, ARROW, AXIOMS> law) {
		List<DOT> components = new ArrayList<DOT>();
		List<Dot<?>> dotComponents = dotComponents(law.getParamSpaces());
		for(Dot<?> dotComponent : dotComponents) {
			components.add(topos.unwrap(dotComponent));
		}
		ProductDiagram<DOT, ARROW> power = topos.getProductDiagram(components);
		List<ARROW> arrowArgs = power.getProjections();
		
		List<Element<?>> elements = new ArrayList<Element<?>>();
		for (int i = 0 ; i < law.getArity() ; i++) {
			Element<?> element = topos.element(dotComponents.get(i), arrowArgs.get(i));
			elements.add(element);
		}
		return law.verify(algebra, elements);
	}

	/*
	 * Check that every required operator is present in the correct arity, using the correct parameter spaces, with no extras
	 */
	private void verifyOps() {
		Map<String, ParsedAxioms.Op> ops = axioms.getOps();
		for (String opName : ops.keySet()) {
			MultiArrow<DOT, ARROW> op = opMap.get(opName);
			if (op == null) {
				throw new IllegalArgumentException("Operator " + opName + " not found in op map supplied to builder");
			}
			ParsedAxioms.Op opDesc = ops.get(opName);
			int expectedArity = opDesc.getArity();
			
			int arity = op.getProductDiagram().getProjections().size();
			if (arity != expectedArity) {
				throw new IllegalArgumentException("Operator " + opName + " in op map has wrong arity "
						+ arity + " != " + expectedArity);
			}
			
			// check the product components match the parameter spaces 
			List<Dot<?>> dotComponents = dotComponents(opDesc.getParamSpaces());
			for (int i = 0 ; i < arity ; i++) {
				DOT component = op.getProductDiagram().getProjections().get(i).getTarget();
				DOT expectedComponent = topos.unwrap(dotComponents.get(i));
				if (component != expectedComponent) {
					throw new IllegalArgumentException("Incorrect parameter space: " + component + " in component #"
							+ (i+1) + " of multiarrow for method " + opName + " ; expected " + expectedComponent);
				}
			}
		}
		if (opMap.size() > ops.size()) { // there are extra operations
			throw new IllegalArgumentException("Surplus operations provided in op map arg to builder");
		}
		// TODO: do an equivalent check for the parameter spaces
	}

	// Parse a list of parameter spaces, filling in the dots - either ours, or the one from the appropriate parameter space
	private List<Dot<?>> dotComponents(List<Algebra<?>> paramSpaces) {
		List<Dot<?>> dotComponents = new ArrayList<Dot<?>>();
		for(Algebra<?> paramSpace : paramSpaces) {
			dotComponents.add((paramSpace == null) ? dotElement : paramSpace.dotCarrier());
		}
		return dotComponents;
	}
	
	AXIOMS build() {		
		algebra = axioms.createProxy(new MyInvocationHandler());
		
		verifyOps(); // (don't need the proxy for this)
		verifyStrictLaws();

		return algebra;
	}

	private class MyInvocationHandler implements MethodHandler {
		public Object invoke(Object proxy, Method method, Method proceed, Object[] args)
				throws Throwable {
			
			String name = method.getName();
			// is this one of the built-in Algebra methods?
			Method algebraMethod = algebraMethods.get(name);
			if (algebraMethod != null) {
				return algebraMethod.invoke(algebraProxy, args);
			}
			
			// invoking getter for a parameter space?
			Algebra<?> parameterSpace = pSpaceMap.get(name);
			if (parameterSpace != null) {
				return parameterSpace;
			}
			
			// assume we're invoking an operator
			List<ARROW> arrows = regularize(elementsToArrows(name, args));
			return invokeOperator(name, arrows);
		}
	}
	
	private Algebra<AXIOMS> algebraProxy = new Algebra<AXIOMS>() {

		public Dot<?> dotCarrier() {
			return dotElement;
		}

		public boolean checkOptionalLaw(String lawName) {
			Boolean holds = verifiedOptionalLaws.get(lawName);
			if (holds == null) {
				ParsedLaw<DOT, ARROW, AXIOMS> law = axioms.getLaws().get(lawName);
				if (law.isStrict()) {
					throw new IllegalArgumentException("? Verifying strict law " + lawName + " as if optional");
				}
				holds = verifyLaw(law);
				verifiedOptionalLaws.put(lawName, holds);
			}
			return holds;
		}

		// A loose concept of equality between arrows, allowing for the fact that they may not have been regularized
		public boolean equality(Object e1, Object e2) {
			@SuppressWarnings("unchecked") ARROW arrow1 = (ARROW)((Element<?>)e1).getArrow();
			@SuppressWarnings("unchecked") ARROW arrow2 = (ARROW)((Element<?>)e2).getArrow();
			List<ARROW> arrows = regularize(pair(arrow1, arrow2));
			return arrows.get(0).equals(arrows.get(1));
		}
	};

	/*
	 * 'Regularize' a list of arrows forming the arguments to an operator. 
	 * This means: making sure they all have the same source.
	 * If any original source is not 1, we premultiply all the global elements so they originate from here,
	 * and we require the other arrows to have the same source.
	 */
	private List<ARROW> regularize(List<ARROW> arrows) {
		for (ARROW arrow : arrows) {
			DOT theSource = arrow.getSource();
			if (theSource != topos.getTerminator()) {
				return regularizeAt(arrows, theSource);
			}
		}
		return arrows; // all global elements, no adjustment necessary
	}

	private List<ARROW> regularizeAt(List<ARROW> arrows, DOT commonSource) {
		List<ARROW> newArrows = new ArrayList<ARROW>();
		ARROW commonToTerminal = topos.getTerminatorDiagram().getConstantArrow(commonSource);
		
		for (ARROW arrow : arrows) {
			DOT theSource = arrow.getSource();
			if (theSource != commonSource) {
				if (theSource == topos.getTerminator()) {
					arrow = arrow.compose(commonToTerminal);
				} else {
					throw new IllegalArgumentException("multiple non-global elements with different sources");
				}
			}
			newArrows.add(arrow);
		}
		return newArrows;
	}
	
	List<ARROW> elementsToArrows(String opName, Object[] elements) {
		Integer arity = axioms.getOps().get(opName).getArity();
		if (arity == null) {
			throw new IllegalArgumentException("Operator " + opName + " not found in parsed axiom scheme");
		}		
		List<ARROW> arrows = new ArrayList<ARROW>();
		if (elements == null) { // No arguments
			if (arity != 0) {
				throw new IllegalArgumentException("Operator " + opName + " called with 0 args; expected "
						+ arity);
			}
		} else {
			if (arity != elements.length) {
				throw new IllegalArgumentException("Operator " + opName +" called with no args;  expected "
						+ arity);
			}
			for (Object arg : elements) {
				@SuppressWarnings("unchecked") ELEMENT elementArg = (ELEMENT)arg;
				@SuppressWarnings("unchecked") ARROW arrowArg = (ARROW)elementArg.getArrow();
				arrows.add(arrowArg);
			}
		}
		return arrows;
	}

	// Extract the common source from a regularized list of arrows. Note the terminator 1 is the default
	private DOT commonSource(List<ARROW> arrows) {
		if (arrows.isEmpty()) {
			return topos.getTerminator();
		} else {
			return arrows.get(0).getSource();
		}
	}
	
	private Element<?> invokeOperator(String opName, List<ARROW> arrows) {
		MultiArrow<DOT, ARROW> op = opMap.get(opName);
		ARROW resultArrow = op.getArrow().compose(op.getProductDiagram().multiplyArrows(commonSource(arrows), arrows));
		return topos.element(dotElement, resultArrow);
	}
	
	// Convenience method - fill in the parameter space map with an empty one if necessary
	public static <
	DOT extends ToposDot<DOT, ARROW>, 
	ARROW extends ToposArrow<DOT, ARROW>,
	AXIOMS extends Algebra<? super AXIOMS>,
	X extends Element<X>
> AXIOMS build(Topos<DOT, ARROW> topos, 
	Dot<X> dotX,
	Class<? extends AXIOMS> template,
	Map<String, Arrow<Product, X>> opMap) {
		return AlgebraBuilder.build(topos, dotX, template, opMap, Collections.<String, Algebra<?>>emptyMap());
	}
	
public static <
	DOT extends ToposDot<DOT, ARROW>, 
	ARROW extends ToposArrow<DOT, ARROW>,
	AXIOMS extends Algebra<? super AXIOMS>,
	X extends Element<X>
> AXIOMS build(Topos<DOT, ARROW> topos, 
	Dot<X> dotX,
	Class<? extends AXIOMS> template,
	Map<String, Arrow<Product, X>> opMap,
	Map<String, Algebra<?>> pSpaceMap) { // TODO: <== shouldn't this be <String, AXIOMS> ?
		return new AlgebraBuilder<DOT, ARROW, AXIOMS, X>(topos, dotX, template, opMap, pSpaceMap).build();
	}
}
