import java.util.Scanner;

 
/** Cette classe est le point d'entree du programme PVNRT.
 * C'est ici que vous devrez ecrire votre projet.
 *
 * <p>Dans le code qui est fourni, les choix suivants ont ete faits :</p>
 *
 * <ul>
 *     <li>Referentiel fenetre VS referentiel particules : vous n'avez normalement pas acces aux informations relatives aux dimensions de la fenetre. Mais soyons simplement conscients que vous exprimerez toutes vos dimensions (positions de particules, vitesses, etc) dans un referentiel qui n'est pas celui de l'affichage. Si vous agrandissez la fenetre d'affichage alors vous aurez la sensation que les particules vont plus vite, mais il n'en est rien. Elles mettent autant de temps a couvrir le parcours de bout en bout de la chambre du piston.</li>
 *     <li>Matrice de particules : Chaque particule est caractarisee par au moins un couple x, y   : coordonnee x, coordonnee y. Vous pouvez ajouter de nouvelles composantes au besoin.
 *         dans votre programme vous serez amenes a tester plusieurs parametres. PENSEZ donc a definir des constantes dans votre programme que vous pourrez modifier a volonte, sans devoir remettre en cause le reste du programme.
 *         Afin de m??moriser l'??tat de l'ensemble des particules vous utiliserez un tableau ?? 2 dimensions. La premi??re dimension sera le choix de la particule, tandis que la seconde contiendra son n-uplet de d?finition.
 *         Par exemple, si double[][] p est ce tableau de particules, alors la case p[1][1] fait reference a l'ordonnee y de la 2eme particule.</li>
 *     <li></li>
 *     <li></li>
 * </ul>
 *
 * <p>Initialement, cette classe ne contient que 2 methodes : </p>
 * <ul>
 *     <li>main : qui a pour role d'initialiser et lancer l'affichage.</li>
 *     <li>next : qui est vide. Elle devra calculer l'instant suivant dans une matrice de particules. 
 *                Vous n'avez pas a faire d'appel a cette methode. Elle est appelee directement par 
 *                l'interface graphique, a chaque fois qu'une nouvelle "image" est a afficher.</li>
 * </ul>
 */
public class PVNRT {
    
	
    //=================================
    //=================================
    //=================================
    // Concepts introduits/a introduire :
    //   * Attribut (Sembe necessaire pour preserver la memoire entre 2 appels de next())
    //   * Gestion d'un tableau de donnees de maniere circulaire
    //   * Travail sur la decomposition de probleme et le choix de signature de methode
    //   * Lecture d'une javadoc
    //   * Utilisation d'un code dont on n'est pas proprietaire
    // 
    // Mis a disposition :
    //   * Trace de courbe
    //   * Trace de courbe avec gestion du tableau de donnees integre
    //   * Processus d'animation des particules en mode pas a pas et en mode animation en continu
    //   * Gestion du piston : expansion, compression et arr?t du piston.
    //   * Export CSV (Sauvegarde le contenu du tableau de Particules + valeur courante de chaque courbe sur une seule ligne).
    //   * 
    //
    // Progression possible : 
    //   1) Faire des tests d'appel des methodes fournies par Affichage
    //   2) SANS ATTRIBUT : Faire une premmiere proposition de calcule de deplacement de particules. Recuperation des particules en debut de Next par appel a getParticules();
    //   3) SANS ATTRIBUT : Ajouter des graphiques avec gestion autonome des donnees pour la pression et l'E Cinetique instantanees.
    //   4) AVEC ATTRIBUT : Ajouter le calcul de pression moyenne
    //   5) AVEC ATTRIBUT : gestion autonome des donnees stockees dans les courbes
    //   
    //
	
	static int indiceVide=0;
	static int indiceDeb=0;
	static final int height=20;
	static final int min=5; 
	static final int width=20;
	static final int delay=10;
	static final String title="Simulation PV=nRT";
	static final double deltaTSimulation=0.01;
	static double pistonVariationModule;
	static final double masse=1;
	static final int nbParticules=500;
	static final double k=100;
	static final double kb=1;
	static double temperature=78;
	static boolean modele;
	static double dseuil=1;
     
    public static void main(String[] args) {
    	
    	Scanner sc=new Scanner(System.in);
    	
    	System.out.println("Type de modèle : (1) reversible isotherme, (2) reversible adiabatique");
    	if(sc.nextInt()==1){
    		
    		modele=true;
    		pistonVariationModule=0.005;
    	
    	}else{
    		
    		modele=false;
    		pistonVariationModule=0.1;
    		
    	}
    	
    	int[][] couleurs=new int[nbParticules][3];
    	double [][] particules=new double [nbParticules][8];//dans l'ordre pour la particule[i] : [i][0] x, [i][1] y, [i][2] vx, [i][3] vy, [i][4] ax, [i][5] ay, [i][6] fx, [i][7] fy
    	for (int i=0;i<particules.length;i++){
    	
    		particules[i][0]=Math.random()*3;
        	particules[i][1]=Math.random()*3;
        	particules[i][2]=Math.random()*3;
        	particules[i][3]=Math.random()*3;
        	
    	}
    	
    	for (int i=0;i<couleurs.length;i++){
        	
    		couleurs[i][0]=255;
    		couleurs[i][1]=255;
    		couleurs[i][2]=255;
        	
    	}
    	
    	Affichage.initAffichage(width, height, min, delay, title, deltaTSimulation, pistonVariationModule);
    	Affichage.setParticules(particules,couleurs);
    	Affichage.addCourbe(courbeTempInstant);
    	Affichage.addCourbe(courbeTempMoy);
    	Affichage.addCourbe(courbePInstant);
    	Affichage.addCourbe(courbePMoy);
    	
    	sc.close();
   
    } 
    
    /** <p>Methode calculant l'instant suivant dans une matrice de particules. </p>
     *  <p>Vous n'avez pas a faire d'appel a cette methode. Elle est appel??e directement par 
     *     l'interface graphique, ?? chaque fois qu'une nouvelle "image" est ?? afficher.</p>
     * <p>C'est ici que vous avez le plus gros de votre travail ?? faire.</p>
     */
    public static void next(){
    	
    	indiceVide = (indiceVide+1)%100;
    	if(indiceVide==indiceDeb){
    		indiceDeb=(indiceDeb+1)%100;
    	}
    	
    	String method =  Affichage.getCalcXYmethod();
        if (method.equals("EULER")) {
            
        	Affichage.setParticules(nextEuler(Affichage.getParticules()));
        	
            } else {
            	
            Affichage.setParticules(nextVerlet(Affichage.getParticules()));
        }
    	
    	nextTempInstant();
    	nextTempMoy();
    	nextPInstant();
    	nextPMoy();
    	ChangeCouleur();
		System.out.println("PV/NkbT="+((pressionMoy[indiceVide]*Affichage.getLarg()*Affichage.getPiston())/(nbParticules*kb*tempMoy[indiceVide])));
    }
    
    public static double[][] nextRebond(double[][] particules){
    	
    	double[][] partic=particules.clone();
    	
    	for(int i=0;i<partic.length;i++){
    		
    		if(partic[i][0]<=3){
    			
    			//partic[i][2]=q*partic[i][2];
    			partic[i][6]=-k*(partic[i][0]-3);
    			
    		}else{
    			
    			if(partic[i][0]>=Affichage.getLarg()-2){
    			
    				partic[i][6]=-k*(partic[i][0]-Affichage.getLarg()+2);
    				//partic[i][2]=q*partic[i][2];
    			
    			}else{
    				
    				partic[i][6]=0;
    			}
    			
    		}
    		
    		if(partic[i][1]<=2){
    			
    			//partic[i][3]=q*partic[i][3];
    			partic[i][7]=-k*(partic[i][1]-2);
        	
    		}else{
    			
    			if(partic[i][1]>=Affichage.getHaut()-2){
    			
    				partic[i][7]=-k*(partic[i][1]-Affichage.getHaut()+2);
    				//partic[i][3]=q*partic[i][3];
    			
    			}else{
    				
    				partic[i][7]=0;
    			}
    			
    		}
    		
    	}
    	
    	return partic;
    }
    
    
    public static double[][] nextChoc(double[][] partic)
	{
		double[][] particules = partic.clone();
		double d=0;

		for(int i=0; i<nbParticules-1;i++)
		{
			
			for(int j=i+1;j<nbParticules;j++)
			{
				d=Math.sqrt(Math.pow(particules[j][0]-particules[i][0],2)+Math.pow(particules[j][1]-particules[i][1],2));
			
				if(d<dseuil)
				{
		
					double nx = (particules[j][0] - particules[i][0])/(d);
					double ny = (particules[j][1] - particules[i][1])/(d);
					double gx = -ny;
					double gy = nx;
					
					double v1n = nx*particules[i][2] + ny*particules[i][3];
					double v1g = gx*particules[i][2] + gy*particules[i][3];
					double v2n = nx*particules[j][2] + ny*particules[j][3];
					double v2g = gx*particules[j][2] + gy*particules[j][3];

					particules[j][2] = nx*v2n +  gx*v1g;
					particules[j][3] = ny*v2n +  gy*v1g;
					particules[i][2] = nx*v1n +  gx*v2g;
					particules[i][3] = ny*v1n +  gy*v2g;
				}
			}
		}
		
		return particules;
	}
    
    public static double[][] nextAccel(double [][] partic){
    	
    	double[][] particules=partic.clone();
    	
    	for(int i=0;i<particules.length;i++){
    	
    		nextRebond(particules);
    		particules[i][4]=(particules[i][6]/masse);
    		particules[i][5]=(particules[i][7]/masse);
    	}
    		
    	return particules;
    }
    
    public static double[][] nextEuler(double[][] partic){
    	
    	double[][] particules=partic.clone();
    	
    	for(int i=0;i<partic.length;i++){
    		
    		particules[i][0]+=particules[i][2]*deltaTSimulation;
    		particules[i][1]+=particules[i][3]*deltaTSimulation;
    		particules[i][2]+=particules[i][4]*deltaTSimulation;
    		particules[i][3]+=particules[i][5]*deltaTSimulation;
    		
    	}
    	
    	nextAccel(particules);
    	
    	return particules;
    }
    
    public static double[][] nextVerlet(double [][] partic){
    	
    	double[][] particules=partic.clone();
    	
    	double temp=0;
    	
    	for(int i=0;i<Affichage.getParticules().length;i++)
			temp+=kb*(Math.pow(Affichage.getParticules()[i][2],2)+Math.pow(Affichage.getParticules()[i][3],2))*masse/nbParticules;
    	
    	double q=1;
		
    	if (modele){
    		
    		if (temp>temperature){
    			
    			q=0.99;
    		
    		}else{
    			if(temp<temperature){
    			
    				q=1.01;
    			
    			}else{
    			
    				q=1;
    			}
    		}
    		
    	}else{
    		
    		q=1;
    	}
    	
    	nextAccel(particules);
    	nextChoc(particules);
    	
    	for(int i=0;i<partic.length;i++){
    		
    		particules[i][2]+=particules[i][4]*deltaTSimulation;
    		particules[i][3]+=particules[i][5]*deltaTSimulation;
    		particules[i][2]=particules[i][2]*q;
    		particules[i][3]=particules[i][3]*q;
    		particules[i][1]+=particules[i][3]*deltaTSimulation;
    		particules[i][0]+=particules[i][2]*deltaTSimulation;
    	}
    	
    	return particules;   
    }
   
    static double[] pressionInstant=new double[100];
    static TraceCourbe courbePInstant = TraceCourbe.getCourbe("Pression Instantanee");
    
    public static void nextPInstant(){
    	
    	double temp=0;
    	
    	for(int i=0;i<Affichage.getParticules().length;i++)
    		temp+=(Math.abs(Affichage.getParticules()[i][6])/(2*Affichage.getHaut())+Math.abs(Affichage.getParticules()[i][7])/(width*2));

    	pressionInstant[indiceVide]=temp;

    	courbePInstant.updtData(pressionInstant, indiceDeb, indiceVide);
    	
    }
    
    static double[] pressionMoy=new double[100];
	static TraceCourbe courbePMoy=TraceCourbe.getCourbe("Pression Moyenne");
	
	public static void nextPMoy(){
		
		double temp=0;
		for (int i=0;i<100;i++){
			temp+=pressionInstant[i]/100;
		}
		
		pressionMoy[indiceVide]=temp;
		
		courbePMoy.updtData(pressionMoy, indiceDeb, indiceVide);
		
	}

	static double[] tempInstant=new double[100];
	static TraceCourbe courbeTempInstant=TraceCourbe.getCourbe("Temperature Instantannee");
	
	public static void nextTempInstant(){
		
		double temp=0;
				
		for(int i=0;i<Affichage.getParticules().length;i++)
			temp+=(kb)*(Math.pow(Affichage.getParticules()[i][2],2)+Math.pow(Affichage.getParticules()[i][3],2))*masse/nbParticules;

		tempInstant[indiceVide]=temp;
				
		courbeTempInstant.updtData(tempInstant, indiceDeb, indiceVide);
		
	}
	
	static double[] tempMoy=new double[100];
	static TraceCourbe courbeTempMoy=TraceCourbe.getCourbe("Temperature Moyenne");
	
	public static void nextTempMoy(){
		
		double temp=0;
		for (int i=0;i<100;i++){
			temp+=tempInstant[i]/100;
		}
		
		tempMoy[indiceVide]=temp;
		
		courbeTempMoy.updtData(tempMoy, indiceDeb, indiceVide);
		
	}
	
	public static void ChangeCouleur()
	{
		int[][] couleur=new int [nbParticules][3];
		for(int i=0;i<nbParticules;i++)
		{
			
		double Vd=Math.sqrt(Math.abs((Math.pow(Affichage.getParticules()[i][2],2)+Math.pow((Affichage.getParticules()[i][3]),2))));
		
		if(Vd<4)
		couleur[i][2]=255;
		couleur[i][1]=0;
		couleur[i][0]=0;
		if(Vd>4&&Vd<8)
		couleur[i][1]=255;
		couleur[i][0]=0;
		if(Vd>8)
		couleur[i][0]=255;
		Affichage.setcolors(couleur);
		
		}
	}
	
 
	
}
