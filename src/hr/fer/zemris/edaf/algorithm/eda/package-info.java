/**
 * Estimation of distribution algorithms (EDAs), sometimes called probabilistic model-building 
 * genetic algorithms (PMBGAs), are stochastic optimization methods that guide the search for the optimum 
 * by building and sampling explicit probabilistic models of promising candidate solutions. 
 * Optimization is viewed as a series of incremental updates of a probabilistic model, 
 * starting with the model encoding the uniform distribution over admissible solutions 
 * and ending with the model that generates only the global optima.
 * 
 * EDAs belong to the class of evolutionary algorithms. The main difference 
 * between EDAs and most conventional evolutionary algorithms is that evolutionary 
 * algorithms generate new candidate solutions using an implicit distribution defined by one 
 * or more variation operators, whereas EDAs use an explicit probability distribution encoded by a 
 * Bayesian network, a multivariate normal distribution, or another model class. Similarly as other evolutionary 
 * algorithms, EDAs can be used to solve optimization problems defined over a number of 
 * representations from vectors to LISP style S expressions, and the quality of candidate 
 * solutions is often evaluated using one or more objective functions.
 * 
 * The general procedure of an EDA is outlined in the following:
 *  t = 0
 *  initialize model M(0) to represent uniform distribution over admissible solutions
 *  while (termination criteria not met)
 *  	P = generate N>0 candidate solutions by sampling M(t)
 *  	F = evaluate all candidate solutions in P
 *  	M(t+1) = adjust_model(P,F,M(t))
 *  	t = t + 1
 *  
 *  Using explicit probabilistic models in optimization allowed EDAs to feasibly 
 *  solve optimization problems that were notoriously difficult for most conventional 
 *  evolutionary algorithms and traditional optimization techniques, such as problems 
 *  with high levels of epistasis. Nonetheless, the advantage of EDAs is also that 
 *  these algorithms provide an optimization practitioner with a series of probabilistic 
 *  models that reveal a lot of information about the problem being solved. This 
 *  information can in turn be used to design problem-specific neighborhood operators 
 *  for local search, to bias future runs of EDAs on a similar problem, or to 
 *  create an efficient computational model of the problem.
 *  
 *  For example, if the population is represented by bit strings of length 4, 
 *  the EDA can represent the population of promising solution using a single vector 
 *  of four probabilities (p1, p2, p3, p4) where each component of p defines 
 *  the probability of that position being a 1. Using this probability vector 
 *  it is possible to create an arbitrary number of candidate solutions.
 *  
 */
package hr.fer.zemris.edaf.algorithm.eda;