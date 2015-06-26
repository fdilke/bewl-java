package com.fdilke.topple.sugar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fdilke.topple.BaseTopos;
import com.fdilke.topple.MultiArrow;
import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.diagrams.ExponentialDiagram;
import com.fdilke.topple.diagrams.ProductDiagram;
import com.fdilke.topple.diagrams.PullbackDiagram;
import com.fdilke.topple.sugar.algebra.schema.HeytingAlgebra;
import com.fdilke.topple.sugar.algebra.schema.Monoid;
import com.fdilke.topple.sugar.algebra.schema.RightGroupAction;
import com.fdilke.topple.sugar.algebra.schema.RightMonoidAction;
import com.fdilke.topple.sugar.algebra.universal.AlgebraBuilder;
import com.fdilke.topple.sugar.dots.DotBiProduct;
import com.fdilke.topple.sugar.dots.DotExponential;
import com.fdilke.topple.sugar.dots.DotProduct;
import com.fdilke.topple.sugar.dots.DotTruth;
import com.fdilke.topple.sugar.dots.DotUnit;
import com.fdilke.topple.sugar.schema.BiProduct;
import com.fdilke.topple.sugar.schema.Exponential;
import com.fdilke.topple.sugar.schema.Product;
import com.fdilke.topple.sugar.schema.Truth;
import com.fdilke.topple.sugar.schema.Unit;
import com.fdilke.topple.util.StandardLogicalOperators;
import com.fdilke.topple.util.StandardLogicalOperatorsTrad;

/**
 * Top layer of "object-oriented" topos API. This provides type-safety and syntactic sugar.
 * We add: classes for dots, typesafe composition, [quantifiers] [truth-value algebra] 
 */

public abstract class Topos<DOT extends ToposDot<DOT, ARROW>, ARROW extends ToposArrow<DOT, ARROW>>
extends BaseTopos<DOT, ARROW>{
	// constant dots/arrows for Unit and truth
	private DotUnit dotUnit;
	private MyDot<Product> dotUnitAsProduct;
	private MyTruthDot dotTruth;
	private Arrow<Product, Truth> trueArrow;
	private HeytingAlgebra<Truth> truthAlgebra;
	
	private static Class<? extends Product> BOUND_PRODUCT_CLASS = (Class<? extends Product>) Product.class;
    private final Factory<DOT, ARROW, Product> PRODUCT_FACTORY = new Factory<DOT, ARROW, Product>() {
        public Product makeInstance(Dot<Product> dot, ARROW arrow) {
            return new MyProduct(dot, arrow);
        }
    };
    
    // Mapping from unit to product and back again
    public Arrow<Product, Unit> productToUnit() {
    	DotUnit dotUnit = dotUnit();
    	return arrow(unwrap(dotUnit.getIdentity()), dotUnitAsProduct(), dotUnit);
    }

    public Arrow<Unit, Product> unitToProduct() {
    	DotUnit dotUnit = dotUnit();
    	return arrow(unwrap(dotUnit.getIdentity()), dotUnit, dotUnitAsProduct());
    }

	// Implementations for dots, arrows, structures
    private class MyDot<E extends Element<E>> implements Dot<E> {
    	private final DOT dot;
    	private final Class<? extends E> boundClass;
    	private final Factory<DOT, ARROW, E> factory;
    	protected final Object structureDiagram; // for dots at products/exponentials/etc

    	public MyDot(DOT dot, Class<E> classE, Object structureDiagram) {
    		this(dot, classE, new DefaultFactory<DOT, ARROW, E>(classE), structureDiagram);
    	}
    	
    	public MyDot(DOT dot, Class<? extends E> boundClass, Factory<DOT, ARROW, E> factory, Object structureDiagram) {
    		this.dot = dot;
    		this.boundClass = boundClass;
    		this.factory = factory;
    		this.structureDiagram = structureDiagram;
    	}

		public MyArrow<E, E> getIdentity() {
			return new MyArrow<E, E>(dot.getIdentity(), this, this);
		}

		public E asElement(ARROW arrow) {
			return factory.makeInstance(this, arrow);
		}

		public Class<? extends E> boundClass() {
			return boundClass;
		}
    }
    
    private class MyArrow<SRC extends Element<SRC>, TGT extends Element<TGT>> implements Arrow<SRC, TGT>{
    	private final ARROW arrow;
    	private final MyDot<SRC> dotSrc;
    	private final MyDot<TGT> dotTgt;
        
    	public MyArrow(ARROW arrow, MyDot<SRC> dotSrc, MyDot<TGT> dotTgt) {
    		this.arrow = arrow;
    		this.dotSrc = dotSrc;
    		this.dotTgt = dotTgt;
    	}
    	
    	public <PRE extends Element<PRE>> Arrow<PRE, TGT> compose(Arrow<PRE, SRC> preArrow) {
			MyArrow<PRE, SRC> thePreArrow = (MyArrow<PRE, SRC>)preArrow;
    		return arrow(arrow.compose(thePreArrow.arrow), thePreArrow.dotSrc, dotTgt);
    	}

		public TGT apply(SRC element) {
			@SuppressWarnings("unchecked") ARROW elementArrow = (ARROW)element.getArrow();
			return dotTgt.asElement(arrow.compose(elementArrow));
		}
		
		public TGT asElement() {
			return dotTgt.asElement(arrow);
		}
		
		@Override public boolean equals(Object o) {
			if (o instanceof MyArrow) {
				@SuppressWarnings("unchecked")
				MyArrow<SRC, TGT> other = (MyArrow<SRC, TGT>)o;
				return arrow.equals(other.arrow);
			} else {
				return false;
			}
		}
		
		@Override public String toString() {
			return arrow.toString();
		}

		public Dot<SRC> source() {
			return dotSrc;
		}

		public Dot<TGT> target() {
			return dotTgt;
		}
    }
    
    // specialized dot types, with structures
    private class MyProductDot extends MyDot<Product> implements DotProduct {
    	private final ProductDiagram<DOT, ARROW> productDiagram;
    	
    	public MyProductDot(ProductDiagram<DOT, ARROW> productDiagram) {
    		super(productDiagram.getProduct(), BOUND_PRODUCT_CLASS, PRODUCT_FACTORY, productDiagram);
    		this.productDiagram = productDiagram;
    	}
    	
		// For multiplying arrows on products
		public Product tuple(List<Element<?>> elements) {
			List<ARROW> arrows = new ArrayList<ARROW>();
			for (Element<?> element : elements) {
				@SuppressWarnings("unchecked") ARROW arrow = (ARROW)element.getArrow();
				arrows.add(arrow);
			}
			
			// This won't work on empty products:fix at some point by requiring source as argument?
			if (arrows.isEmpty()) {
				throw new IllegalArgumentException("Empty products not supported: can't determine source");
			}
			DOT source = arrows.get(0).getSource();
			
			ARROW productArrow = productDiagram.multiplyArrows(source, arrows);
			return (Product) asElement(productArrow); 
		}
		
		public <SRC extends Element<SRC>>
		Arrow<SRC, Product> multiply(Dot<SRC> dotSrc, final List<Arrow<SRC, ?>> arrows) {
	        return arrow(dotSrc, this,
	        		new Lambda<SRC, Product>() {
	            public Product lambda(SRC src) {
	            	List<Element<?>> elements = new ArrayList<Element<?>>();
	            	for (Arrow<SRC, ?> arrow : arrows) {
	            		elements.add(arrow.apply(src));
	            	}
	            	return tuple(elements);
	            }
	        });
		}

		public <COMPONENT extends Element<COMPONENT>> Arrow<Product, COMPONENT> projection(
				int index, Dot<COMPONENT> component) {
			ARROW projection = productDiagram.getProjections().get(index);
			return arrow(projection, this, component);
		}
	}
    
    private class MyBiProductDot<LEFT extends Element<LEFT>, RIGHT extends Element<RIGHT>>
	extends MyDot<BiProduct<LEFT,RIGHT>>
	implements DotBiProduct<LEFT, RIGHT>{
    	private final Dot<LEFT> dotLeft;
    	private final Dot<RIGHT> dotRight;
    	private final ProductDiagram<DOT, ARROW> productDiagram;
    	
		@SuppressWarnings("unchecked")
		MyBiProductDot(Dot<LEFT> dotLeft, Dot<RIGHT> dotRight,
				ProductDiagram<DOT, ARROW> productDiagram, Factory<DOT, ARROW, BiProduct<LEFT, RIGHT>> factory) {
	        super(productDiagram.getProduct(),
					(Class<? extends BiProduct<LEFT, RIGHT>>) (Object) BiProduct.class,
					factory,
					productDiagram);
	        this.dotLeft = dotLeft;
	        this.dotRight = dotRight;
	        this.productDiagram = productDiagram;
		}
		
		// ...and biproducts
		public BiProduct<LEFT, RIGHT> pair(LEFT left, RIGHT right) {
			@SuppressWarnings("unchecked") ARROW leftArrow = (ARROW)left.getArrow();
			@SuppressWarnings("unchecked") ARROW rightArrow = (ARROW)right.getArrow();
			
			ARROW productArrow = productDiagram.multiplyArrows(leftArrow.getSource(), BaseTopos.pair(leftArrow, rightArrow));
			BiProduct<LEFT, RIGHT> productElement = (BiProduct<LEFT, RIGHT>) asElement(productArrow); 
	        return productElement;
	    }

		public Arrow<BiProduct<LEFT, RIGHT>, LEFT> leftProjection() {
			return arrow(productDiagram.getProjections().get(0), this, dotLeft);
		}

		public Arrow<BiProduct<LEFT, RIGHT>, RIGHT> rightProjection() {
			return arrow(productDiagram.getProjections().get(1), this, dotRight);
		}
	}

	private class MyExponentialDot<SRC extends Element<SRC>, TGT extends Element<TGT>> 
	extends MyDot<Exponential<SRC, TGT>>
	implements DotExponential<SRC, TGT> {
		@SuppressWarnings("unchecked")
		public MyExponentialDot(ExponentialDiagram<DOT, ARROW> diagram, Factory<DOT, ARROW, Exponential<SRC, TGT>> factory) {
			super(exponentialDot(diagram),
					(Class<? extends Exponential<SRC, TGT>>) (Object) Exponential.class,
					factory,
					diagram);
		}
		
		public <E extends Element<E>> Arrow<E, Exponential<SRC, TGT>> transpose(
				Arrow<BiProduct<E, SRC>, TGT> wrappedMultiArrow) {
			if (structureDiagram == null || !(structureDiagram instanceof ExponentialDiagram)) {
				throw new IllegalArgumentException("Exponential diagram not found");
			}
			@SuppressWarnings("unchecked") ExponentialDiagram<DOT, ARROW>  expDiagram = 
				(ExponentialDiagram<DOT, ARROW>)structureDiagram;
			MyArrow<BiProduct<E, SRC>, TGT> myMultiArrow = (MyArrow<BiProduct<E, SRC>, TGT>)wrappedMultiArrow;
			MultiArrow<DOT, ARROW> multiArrow = new MultiArrow<DOT, ARROW>(biproductDiagram(myMultiArrow.dotSrc),
					myMultiArrow.arrow); 
			
			// To get the dot for F, a very dirty trick:
			MyDot<BiProduct<E, SRC>> dotProduct = (MyDot<BiProduct<E, SRC>>)wrappedMultiArrow.source();
			MyBiProduct<E, SRC> dudProduct = (MyBiProduct<E, SRC>)dotProduct.asElement(null);
			Dot<E> dotE = dudProduct.dotLeft;
			
			return arrow(expDiagram.getTranspose(multiArrow), dotE, this);
		}

		public Arrow<Unit, Exponential<SRC, TGT>> name(final Arrow<SRC, TGT> arrow) {
			Dot<BiProduct<Unit, SRC>> biProduct = product(dotUnit(), arrow.source());
			Arrow<BiProduct<Unit, SRC>, TGT> multiArrow = arrow(biProduct, arrow.target(),
	        		new Lambda<BiProduct<Unit, SRC>, TGT>() {
	            public TGT lambda(BiProduct<Unit, SRC> biProduct) {
	            	SRC src = biProduct.right();
	            	return arrow.apply(src);
	            }
	        });
			return transpose(multiArrow);
		}
	}
    
    private class MyBiProduct<LEFT extends Element<LEFT>, RIGHT extends Element<RIGHT>> 
    extends BiProduct<LEFT, RIGHT> {
    	private final MyDot<LEFT> dotLeft;
        private final MyDot<RIGHT> dotRight;
    	private final MyDot<BiProduct<LEFT, RIGHT>> dotProduct;
    	 
        public MyBiProduct(Dot<LEFT> dotLeft, Dot<RIGHT> dotRight,
        		Dot<BiProduct<LEFT, RIGHT>> dotProduct, ARROW arrow) {
        	this.dotLeft = (MyDot<LEFT>) dotLeft;
        	this.dotRight = (MyDot<RIGHT>) dotRight;
			this.dotProduct = (MyDot<BiProduct<LEFT, RIGHT>>) dotProduct;
			setArrow(arrow);
		}

		@Override public LEFT left() {
			@SuppressWarnings("unchecked")
			ProductDiagram<DOT, ARROW> productDiagram = (ProductDiagram<DOT, ARROW>)dotProduct.structureDiagram;
			@SuppressWarnings("unchecked") ARROW arrow = (ARROW) getArrow();
			
			ARROW left = productDiagram.getProjections().get(0);
			return dotLeft.asElement(left.compose(arrow));
        }

        @Override public RIGHT right() {
			@SuppressWarnings("unchecked")
			ProductDiagram<DOT, ARROW> productDiagram = (ProductDiagram<DOT, ARROW>)dotProduct.structureDiagram;
			@SuppressWarnings("unchecked") ARROW arrow = (ARROW) getArrow();
			
			ARROW right = productDiagram.getProjections().get(1);
			return dotRight.asElement(right.compose(arrow));        
		}  
    }

    private class MyProduct extends Product {
    	private final MyDot<Product> dotProduct;
    	 
        public MyProduct(Dot<Product> dotProduct, ARROW arrow) {
 			this.dotProduct = (MyDot<Product>) dotProduct;
			setArrow(arrow);
		}

		@Override
		public <E extends Element<E>> E component(int index, Dot<E> dot) {
			MyDot<E> myDot = (MyDot<E>) dot;
			@SuppressWarnings("unchecked")
			ProductDiagram<DOT, ARROW> productDiagram = (ProductDiagram<DOT, ARROW>)dotProduct.structureDiagram;
			@SuppressWarnings("unchecked") ARROW arrow = (ARROW) getArrow();
			
			ARROW projection = productDiagram.getProjections().get(index);
			return myDot.asElement(projection.compose(arrow));
        }
    }
     
    private class MyExponential<SRC extends Element<SRC>, TGT extends Element<TGT>>
    extends Exponential<SRC, TGT> {
    	private final ExponentialDiagram<DOT, ARROW> expDiagram; 
    	private final MyDot<TGT> dotTarget; 
    	
    	public MyExponential(ExponentialDiagram<DOT, ARROW> expDiagram, MyDot<TGT> dotTarget, ARROW arrow) {
    		this.expDiagram = expDiagram;
    		this.dotTarget = dotTarget;
    		setArrow(arrow);
    	}
    	
		@Override
		public TGT evaluate(SRC src) {
			MultiArrow<DOT, ARROW> evaluation = expDiagram.getEvaluation();
			ProductDiagram<DOT, ARROW> productDiagram = evaluation.getProductDiagram();
			@SuppressWarnings("unchecked")
			List<ARROW> arrows = pair((ARROW)arrow, (ARROW)src.arrow);
			@SuppressWarnings("unchecked")
			ARROW productArrow = productDiagram.multiplyArrows((DOT)arrow.getSource(), arrows);
			ARROW transposedArrow = evaluation.getArrow().compose(productArrow);
			return dotTarget.asElement(transposedArrow);
		}
    }
    
    private class MyUnitDot extends MyDot<Unit> implements DotUnit {
    	public MyUnitDot() {
    		super(getTerminator(), Unit.class, getTerminatorDiagram());
    	}

		public <X extends Element<X>> Arrow<X, Unit> constant(Dot<X> dotX) {
			ARROW constantArrow = getTerminatorDiagram().getConstantArrow(unwrap(dotX));
			return arrow(constantArrow, dotX, dotUnit());
		}
    }
    
    private class MyTruthDot extends MyDot<Truth> implements DotTruth {
    	public MyTruthDot() {
    		super(getOmega(), Truth.class, null);
    	}

		public <S extends Element<S>, T extends Element<T>> Arrow<T, Truth> characteristic(
				Arrow<S, T> monic) {
			PullbackDiagram<DOT, ARROW> pullback = getSubobjectClassifier().
								pullbackMonic(unwrap(monic));
	        ARROW charArrow = pullback.getEast(); // characteristic map
	        return arrow(charArrow, monic.target(), dotTruth());
		}
    }

    private class MyQuantifiers<X extends Element<X>> implements Quantifiers<X> {
    	private final DotExponential<X, Truth> powerX;
    	private final Arrow<Exponential<X, Truth>, Truth> forAll;
    	
    	public MyQuantifiers(Dot<X> dotX) {
    		powerX = exponential(dotX, dotTruth());
    		
    	    // build the quantifier "forAll x", which maps omega^x to omega
    		// it is: the characteristic map of the name of truth 1 -> o^x 
    		Arrow<X, Truth> trueX = trueArrow().compose(unitToProduct())
    							.compose(dotUnit().constant(dotX));
    		Arrow<Unit, Exponential<X, Truth>> nameOfTrue = powerX.name(trueX);
    		forAll = dotTruth().characteristic(nameOfTrue);
    	}

		public Arrow<Exponential<X, Truth>, Truth> forAll() {
			return forAll;
		}

		public DotExponential<X, Truth> power() {
			return powerX;
		}
    }
    
    // Wrappers, unwrappers, builders
    
    public <E extends Element<E>> Dot<E> dot(DOT dot, Class<E> classE) {
        return new MyDot<E>(dot, classE, null);
    }
    
    public <SRC extends Element<SRC>, TGT extends Element<TGT>> Arrow<SRC, TGT> arrow(ARROW arrow, 
    		Dot<SRC> src, Dot<TGT> tgt) {
    	MyDot<SRC> dotSrc = (MyDot<SRC>)src;
    	MyDot<TGT> dotTgt = (MyDot<TGT>)tgt;
        return new MyArrow<SRC, TGT>(arrow, dotSrc, dotTgt);
    }
    
    public <E extends Element<E>> DOT unwrap(Dot<E> theDot) {
    	MyDot<E> dot = (MyDot<E>) theDot;
    	return dot.dot;
    }
    
    public <E extends Element<E>> ARROW unwrap(Element<E> element) {
    	@SuppressWarnings("unchecked") ARROW arrow = (ARROW)element.arrow;
    	return arrow;
    }
    
    public <SRC extends Element<SRC>, TGT extends Element<TGT>> ARROW unwrap(Arrow<SRC, TGT> theArrow) {
    	MyArrow<SRC, TGT> arrow = (MyArrow<SRC, TGT>)theArrow;
    	return arrow.arrow;
    }
    
	public <E extends Element<E>> E element(Dot<E> theDot, ARROW arrow) {
		MyDot<E> dot = (MyDot<E>)theDot;
		return dot.asElement(arrow);
	}
    
    /*
     * Build an arrow with the 'lambda' construction. This is really the heart of Bile.
     */
	public <SRC extends Element<SRC>, TGT extends Element<TGT>>
	Arrow<SRC, TGT> arrow(
		Dot<SRC> src, Dot<TGT> tgt,
		Lambda<SRC, TGT> lambda) {
		MyDot<SRC> dotSrc = (MyDot<SRC>)src; 
		MyDot<TGT> dotTgt = (MyDot<TGT>)tgt; 
        MyArrow<SRC, SRC> identity = dotSrc.getIdentity();
        SRC element = dotSrc.asElement(identity.arrow);
        Element<TGT> result = lambda.lambda(element);
        @SuppressWarnings("unchecked") ARROW arrow = (ARROW)result.getArrow();
        return new MyArrow<SRC, TGT>(arrow, dotSrc, dotTgt);
	}
	
    public <LEFT extends Element<LEFT>, RIGHT extends Element<RIGHT>> DotBiProduct<LEFT, RIGHT>
    	product(final Dot<LEFT> left, final Dot<RIGHT> right) {
    	MyDot<LEFT> dotLeft = (MyDot<LEFT>)left;
    	MyDot<RIGHT> dotRight = (MyDot<RIGHT>)right;
    	
        List<DOT> components = pair(dotLeft.dot, dotRight.dot);
        ProductDiagram<DOT, ARROW> diagram = getProductDiagram(components);
        Factory<DOT, ARROW, BiProduct<LEFT, RIGHT>> factory = new Factory<DOT, ARROW, BiProduct<LEFT, RIGHT>>() {
            public BiProduct<LEFT, RIGHT> makeInstance(Dot<BiProduct<LEFT, RIGHT>> dot, ARROW arrow) {
                return new MyBiProduct<LEFT,RIGHT>(left, right, dot, arrow);
            }
        };
        return new MyBiProductDot<LEFT,RIGHT>(left, right, diagram, factory);
    }
            
    public DotProduct product(List<Dot<?>> components) {
	    List<DOT> rawDots = new ArrayList<DOT>();
	    for(Dot<?> dot : components) {
	    	MyDot<?> myDot = (MyDot<?>)dot;
	    	rawDots.add(myDot.dot);
	    }
	    return new MyProductDot(getProductDiagram(rawDots));
	}    

	public <SRC extends Element<SRC>, TGT extends Element<TGT>> DotExponential<SRC, TGT>
    exponential(Dot<SRC> src, Dot<TGT> tgt) {
    	MyDot<SRC> dotSrc = (MyDot<SRC>) src; 
    	final MyDot<TGT> dotTgt = (MyDot<TGT>) tgt;
    	
        final ExponentialDiagram<DOT, ARROW> diagram = getExponentialDiagram(dotTgt.dot, dotSrc.dot);
        Factory<DOT, ARROW, Exponential<SRC, TGT>> factory = new Factory<DOT, ARROW, Exponential<SRC, TGT>>() {
            public Exponential<SRC, TGT> makeInstance(Dot<Exponential<SRC, TGT>> dot, ARROW arrow) {
                return new MyExponential<SRC, TGT>(diagram, dotTgt, arrow);
            }
        };
        return new MyExponentialDot<SRC, TGT>(diagram, factory);
    }
		
	public <
		SRC extends Element<SRC>,
		TGT extends Element<TGT>
	> Arrow<Unit, Exponential<SRC, TGT>> name(DotExponential<SRC, TGT> exponentialDot, final Arrow<SRC, TGT> arrow) {
		Dot<BiProduct<Unit, SRC>> dotProduct = product(dotUnit(), arrow.source());
		return exponentialDot.transpose(arrow(dotProduct, arrow.target(), 
			new Lambda<BiProduct<Unit, SRC>, TGT>() {
				public TGT lambda(BiProduct<Unit, SRC> productElement) {
					return arrow.apply(productElement.right());
				}
		}));
	}

    // Wrapping and unwrapping biarrows: given a "raw" biarrow, compose it so it originates from a dot product
    
    public <E extends Element<E>, F extends Element<F>, G extends Element<G>>
    Arrow<BiProduct<E, F>, G> biArrow(
    		MultiArrow<DOT, ARROW> multiArrow,
    		Dot<BiProduct<E, F>> dotProduct, 
    		Dot<G> dotTarget) {
       	ARROW iso = canonicalIso(biproductDiagram(dotProduct), multiArrow.getProductDiagram());
    	return arrow(multiArrow.getArrow().compose(iso), dotProduct, dotTarget);
    }
    
	public <
		F extends Element<F>,
		G extends Element<G>,
		H extends Element<H>
	>
	MultiArrow<DOT, ARROW> unwrapBiArrow(Arrow<BiProduct<F, G>, H>multiArrow) {
		return new MultiArrow<DOT, ARROW>(biproductDiagram(multiArrow.source()), unwrap(multiArrow));
	}

    // wrapping and unwrapping multiarrows: given a "raw" multiarrow, compose it so it originates from a dot product
    
    public <TGT extends Element<TGT>>
    Arrow<Product, TGT> multiArrow(
    		MultiArrow<DOT, ARROW> multiArrow,
    		Dot<Product> dotProduct, 
    		Dot<TGT> dotTarget) {
       	ARROW iso = canonicalIso(productDiagram(dotProduct), multiArrow.getProductDiagram());
    	return arrow(multiArrow.getArrow().compose(iso), dotProduct, dotTarget);
    }
    
	public <H extends Element<H>>
	MultiArrow<DOT, ARROW> unwrapMultiArrow(Arrow<Product, H>multiArrow) {
		return new MultiArrow<DOT, ARROW>(productDiagram(multiArrow.source()), unwrap(multiArrow));
	}
	
	// operator utilities
	// like nullaryOp() but at Bile level
	public <TGT extends Element<TGT>> 
	Arrow<Product, TGT> nullaryOp(ARROW arrow, Dot<TGT> dotTgt) {
		Dot<Product> dotProduct = dotUnitAsProduct();
		return multiArrow(nullaryOp(arrow), dotProduct, dotTgt);
	}
	
	// like unaryOp() but at Bile level
	public <TGT extends Element<TGT>> 
	Arrow<Product, TGT> unaryOp(ARROW arrow, Dot<TGT> dotTgt) {
		MultiArrow<DOT, ARROW> unaryOp = unaryOp(arrow);
		ProductDiagram<DOT, ARROW> diagram = unaryOp.getProductDiagram();
		Dot<Product> dotProduct = new MyProductDot(diagram); 
		return multiArrow(unaryOp, dotProduct, dotTgt);
	}
	
	// standard dots for unit and truth. Note we treat unit-as-product separately (can get round this?)
	
    public DotUnit dotUnit() { // this has to be lazily instantiated :(
    	if (dotUnit == null) {
    		 dotUnit = new MyUnitDot();
    	}
    	return dotUnit;
    }

    public Dot<Product> dotUnitAsProduct() { // this has to be lazily instantiated :(
    	if (dotUnitAsProduct == null) {
    		dotUnitAsProduct = new MyProductDot(getTerminatorDiagram());
       	}
    	return dotUnitAsProduct;
    }
    
    public DotTruth dotTruth() { // this has to be lazily instantiated :(
    	if (dotTruth == null) {
    		 dotTruth = new MyTruthDot();
    	}
    	return dotTruth;
    }
    
    public Arrow<Product, Truth> trueArrow() {
    	if (trueArrow == null) {
    		trueArrow = nullaryOp(getSubobjectClassifier().getTruth(), dotTruth());
    	}
    	return trueArrow;
    }
    
    public <E extends Element<E>> Arrow<E, Unit> constantArrow(Dot<E> theDotE) {
    	MyDot<E> dotE = (MyDot<E>) theDotE;
        ARROW constantArrow = getTerminatorDiagram().getConstantArrow(dotE.dot);
        @SuppressWarnings("unchecked")
		Arrow<E, Unit> constant = new MyArrow<E, Unit>(constantArrow, dotE, (MyUnitDot)dotUnit());
        return constant;
    }
    
    public static interface Factory< 
    	DOT extends ToposDot<DOT, ARROW>,
    	ARROW extends ToposArrow<DOT, ARROW>,
    	E extends Element<E>
    > {
        public E makeInstance(Dot<E> dot, ARROW arrow);
    }
    
    public static class DefaultFactory<
		DOT extends ToposDot<DOT, ARROW>,
		ARROW extends ToposArrow<DOT, ARROW>,
		E extends Element<E>
    > implements Factory<DOT, ARROW, E> {
        private final Class<E> classE;

        public DefaultFactory(Class<E> classE) {
            this.classE = classE;
        }

        public E makeInstance(Dot<E> dot, ARROW arrow) {
            try {
                E e = classE.newInstance();
                e.setArrow(arrow);
                return e;
            } catch (Exception ex) {
                throw new IllegalArgumentException("Can't instantiate element class " + classE, ex);
            }
        }
    }

    // Extracting diagrams from products and biproducts
    
    @SuppressWarnings("unchecked")
	public <E extends Element<E>, F extends Element<F>>  
    ProductDiagram<DOT, ARROW> biproductDiagram(Dot<BiProduct<E, F>> dotProduct) {
    	MyDot<BiProduct<E, F>> myDotProduct = (MyDot<BiProduct<E, F>>)dotProduct;
    	return (ProductDiagram<DOT, ARROW>) myDotProduct.structureDiagram;
    }

    @SuppressWarnings("unchecked")
	public ProductDiagram<DOT, ARROW> productDiagram(Dot<Product> dotProduct) {
    	MyDot<Product> myDotProduct = (MyDot<Product>)dotProduct;
    	return (ProductDiagram<DOT, ARROW>) myDotProduct.structureDiagram;
    }
    
	public HeytingAlgebra<Truth> truthAlgebra() {
		if (truthAlgebra == null) {
			truthAlgebra = buildTruthAlgebra(Language.NEW);
		}
		return truthAlgebra;
	}
	
	public HeytingAlgebra<Truth> buildTruthAlgebra(Language language) {
		switch(language) {
		case TRAD: return buildTruthAlgebraTrad();
		case NEW:  return buildTruthAlgebra();
		default: throw new IllegalArgumentException("unknown language type " + language);
		}
	}

	public HeytingAlgebra<Truth> buildTruthAlgebra() {
		StandardLogicalOperators<DOT, ARROW> logicalOperators = new StandardLogicalOperators<DOT, ARROW>(this);
//		Dot<Product> dotSquare = logicalOperators.getOmegaSquared();
	    Map<String, Arrow<Product, Truth>> opMap = new HashMap<String, Arrow<Product, Truth>>();
		opMap.put("TRUE", logicalOperators.getTrue());
		opMap.put("FALSE", logicalOperators.getFalse());
		opMap.put("or", logicalOperators.getOr());
		opMap.put("and", logicalOperators.getAnd());
		opMap.put("implies", logicalOperators.getImplies());
		
		@SuppressWarnings("unchecked") Class<? extends HeytingAlgebra<Truth>>
		axiomsClass = (Class<? extends HeytingAlgebra<Truth>>) (Object) HeytingAlgebra.class;

		return AlgebraBuilder.build(this, dotTruth(), axiomsClass, opMap);
	}
	
	public HeytingAlgebra<Truth> buildTruthAlgebraTrad() {
		StandardLogicalOperatorsTrad<DOT, ARROW> logicalOperators = new StandardLogicalOperatorsTrad<DOT, ARROW>(this);
		Dot<Product> dotSquare = new MyProductDot(logicalOperators.getOmegaSquared());
	    Map<String, Arrow<Product, Truth>> opMap = new HashMap<String, Arrow<Product, Truth>>();
		opMap.put("TRUE", nullaryOp(logicalOperators.getTrue(), dotTruth()));
		opMap.put("FALSE", nullaryOp(logicalOperators.getFalse(), dotTruth()));
		opMap.put("or", multiArrow(logicalOperators.getOr(), dotSquare, dotTruth()));
		opMap.put("and", multiArrow(logicalOperators.getAnd(), dotSquare, dotTruth()));
		opMap.put("implies", multiArrow(logicalOperators.getImplies(), dotSquare, dotTruth()));
		
		@SuppressWarnings("unchecked") Class<? extends HeytingAlgebra<Truth>>
		axiomsClass = (Class<? extends HeytingAlgebra<Truth>>) (Object) HeytingAlgebra.class;

		return AlgebraBuilder.build(this, dotTruth(), axiomsClass, opMap);
	}
	
	// TODO: fix this
	public <
	SCALAR extends Element<SCALAR>,
	VECTOR extends Element<VECTOR>
	> RightGroupAction<SCALAR, VECTOR> automorphisms(
		Class<SCALAR> classScalar,
		Dot<VECTOR> dotVector) {
		// sketch of attempt to calculate object of two-sided inverses...
//		Arrow<Product<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>>, Truth> areInverse = arrow(dotProduct, dotTruth(),
//                new Lambda<Product<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>>, Truth>() {
//            public Truth lambda(Product<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>> pair) {
//            	Exponential<VECTOR, VECTOR> left = pair.left();
//            	Exponential<VECTOR, VECTOR> right = pair.right();
//            	return null;
//            }
//		});		
		return null;
	}
	
	public <
	SCALAR extends Element<SCALAR>,
	VECTOR extends Element<VECTOR>
	> RightMonoidAction<SCALAR, VECTOR> endomorphismsNew(
		Class<SCALAR> classScalar,
		Dot<VECTOR> dotVector) {
		DotExponential<VECTOR, VECTOR> exponentialDot = exponential(dotVector, dotVector);
		Dot<BiProduct<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>>> dotExpExp 
			= product(exponentialDot, exponentialDot);
		Dot<BiProduct<BiProduct<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>>, VECTOR>> dotExpExpVec 
			= product(dotExpExp, dotVector);
		Arrow<BiProduct<BiProduct<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>>, VECTOR>, VECTOR> 
			applyBoth = 				
				arrow(dotExpExpVec, dotVector,
                new Lambda<BiProduct<BiProduct<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>>, VECTOR>, VECTOR>() {
            public VECTOR lambda(BiProduct<BiProduct<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>>, VECTOR> pair) {
            	BiProduct<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>> scalars = pair.left();
            	Exponential<VECTOR, VECTOR> scalar1 = scalars.left();
            	Exponential<VECTOR, VECTOR> scalar2 = scalars.right();
            	VECTOR vector = pair.right();
            	return scalar2.evaluate(scalar1.evaluate(vector));
            }
		});
		Dot<SCALAR> dotScalar = dot(unwrap(exponentialDot), classScalar);
		Arrow<BiProduct<Exponential<VECTOR, VECTOR>, Exponential<VECTOR, VECTOR>>, Exponential<VECTOR, VECTOR>> 
			multiply = exponentialDot.transpose(applyBoth);		
		MultiArrow<DOT, ARROW> multiMultiply = unwrapBiArrow(multiply); 
		Arrow<Unit, Exponential<VECTOR, VECTOR>> unit = name(exponentialDot, dotVector.getIdentity());
		Monoid<SCALAR> monoid = Monoid.<DOT, ARROW, SCALAR>build(this, dotScalar, unwrap(unit), multiMultiply);

		Arrow<BiProduct<VECTOR, Exponential<VECTOR, VECTOR>>, VECTOR> 
		applyScalar = arrow(product(dotVector, exponentialDot), dotVector, 
			new Lambda<BiProduct<VECTOR, Exponential<VECTOR, VECTOR>>, VECTOR>() {
				public VECTOR lambda(
						BiProduct<VECTOR, Exponential<VECTOR, VECTOR>> element) {
					VECTOR vector = element.left();
					Exponential<VECTOR, VECTOR> scalar = element.right();
					return scalar.evaluate(vector);
				}
			});
		return monoid.action(this, dotVector, unwrapBiArrow(applyScalar));
	}
	
	public <
		SCALAR extends Element<SCALAR>,
		VECTOR extends Element<VECTOR>
	> RightMonoidAction<SCALAR, VECTOR> endomorphismsTrad(
			Class<SCALAR> classScalar,
			Dot<VECTOR> dotVector) {
		DOT dot = unwrap(dotVector);
		ExponentialDiagram<DOT, ARROW> power = getExponentialDiagram(dot, dot);
		MultiArrow<DOT, ARROW> eval = power.getEvaluation(); // evaluation map C^B x B -> C, where B=C=dot
	    List<ARROW> expProjections = eval.getProductDiagram().getProjections();
	    DOT powerDot = expProjections.get(0).getTarget();
	    ARROW unit = getName(dot.getIdentity(), power);
	
		ProductDiagram<DOT, ARROW> square = product(powerDot, powerDot);
		ProductDiagram<DOT, ARROW> diagram = product(square.getProduct(), dot);
		DOT diagramDot = diagram.getProduct();
		ARROW getF = square.getProjections().get(0).compose(diagram.getProjections().get(0));
		ARROW getG = square.getProjections().get(1).compose(diagram.getProjections().get(0));
		ARROW getX = diagram.getProjections().get(1);		
		
		ARROW getFX = eval.getArrow().compose(BaseTopos.multiply(eval.getProductDiagram(), diagramDot, getF, getX));
		ARROW getGFX = eval.getArrow().compose(BaseTopos.multiply(eval.getProductDiagram(), diagramDot, getG, getFX));
		MultiArrow<DOT, ARROW> multiArrow = new MultiArrow<DOT, ARROW>(diagram, getGFX);	
		MultiArrow<DOT, ARROW> multiply = new MultiArrow<DOT, ARROW>(square, power.getTranspose(multiArrow));
		Dot<SCALAR> dotScalar = dot(powerDot, classScalar);
		Monoid<SCALAR> monoid = Monoid.<DOT, ARROW, SCALAR>build(this, dotScalar, unit, multiply); 
		
		// Construct scalar multiplication SxM ==> M by twisting evaluation
		ProductDiagram<DOT, ARROW> s_m = product(dot, powerDot);
		ARROW scalarMultiply = eval.getArrow().compose(twist(s_m, eval.getProductDiagram()));
		return monoid.action(this, dotVector, new MultiArrow<DOT, ARROW>(s_m, scalarMultiply));
	}
	
	/*
	 * switch which lets us invoke the endomorphism calculation for either language
	 */
	public <
	SCALAR extends Element<SCALAR>,
	VECTOR extends Element<VECTOR>
	> RightMonoidAction<SCALAR, VECTOR> endomorphisms(
		Class<SCALAR> classScalar,
		Dot<VECTOR> dotVector,
		Language language) {
		switch(language) {
			case TRAD:	return endomorphismsTrad(classScalar, dotVector);
			case NEW:	return endomorphismsNew(classScalar, dotVector);
			default: throw new IllegalArgumentException("unknown language " + language);
		}
	}

	// TODO: add proper tests for this at abstract level
	public <X extends Element<X>> Quantifiers<X> quantifiers(Dot<X> dotX) {
		return new MyQuantifiers<X>(dotX);
	}
	
	// TODO: calculate the endomorphisms and automorphisms of an algebra. Good to only use the DSL here?
	// TODO: make the right actions over a monoid into a topos
	// TODO: add unit tests for quantifiers and constant() and name()
}
