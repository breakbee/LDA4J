LDA4J
=====

Latent Dirichlet Allocation for Java  

Usage:  
java -jar lda4j.jar corpus.data [topic_num] [iter_num] [summary] [theta] [phi]  

**corpus.data**  
Corpus (separator : space)  

Sample:  
Neural Network probabilistic Model  
Recently Deep Learning are ...  

**topic_num(optional)**  
topic Number (default=2)  
**iter_num(optional)**  
iteration Number of Gibbs Sampling (default=100)  
**summary(optional)**  
file path for LDA's result summary (default=summary.txt)  
**theta(optional)**  
file path for parameter theta's matrix (default=theta.csv)  
**phi(optional)**  
file path for parameter phi's matrix (default=phi.csv)  

Sample:  
java -jar lda4j.jar corpus.data 2 100 summary.txt theta.csv phi.csv  