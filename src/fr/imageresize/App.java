package fr.imageresize;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class App {
	private static File rootFolder;
	private static File compressFolder;
	private static JFileChooser jFileChooser = new JFileChooser();
	private static JProgressBar progressBar = new JProgressBar(0, 100);
	private static JLabel label2 = new JLabel("");
	private static ImageExtFilter fileFilter = new ImageExtFilter(new String[]{"jpg", "jpeg"});
	private static int cpt = 0;
	
	public App(){
		
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		// Fenêtre
		final JFrame window = new JFrame();
		window.setTitle("Image Resize");
		//window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setSize(400, 380);
		window.setLocationRelativeTo(null);
		window.setResizable(false);
		
		// Panel
		final JPanel panel = new JPanel();
		
		// Chargement d'Eros
		JLabel picLabel = new JLabel(new ImageIcon(App.class.getResource("Eros.jpg")));
		panel.add(picLabel);
		
		// Label
		final JLabel label = new JLabel("");
		
		// Bouton
		final JButton boutonChoisir = new JButton("Choisir un répertoire...");
		boutonChoisir.addActionListener(new ActionListener() {	
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// Si un répertoire a été choisi dans le jFileChooser
				if (openDialog(panel) == JFileChooser.APPROVE_OPTION) {
					// Récupère le chemin sélectionné
					rootFolder = jFileChooser.getSelectedFile();
					File[] listFiles = rootFolder.listFiles(fileFilter);
					// Si des images sont trouvées dans le dossier sélectionner
					if (listFiles.length > 0) {
						window.setSize(400, 420);
						boutonChoisir.setVisible(false);
						String folderPath = jFileChooser.getSelectedFile().getAbsolutePath();
						label.setText("Dossier: " + folderPath);
						panel.add(label);
						
						// Progress bar
						progressBar.setStringPainted(true);
						progressBar.setValue(0);
						progressBar.repaint();
						panel.add(progressBar);
						
						panel.add(label2);
						resizeFiles(listFiles);
						progressBar.setValue(progressBar.getMaximum());
						JPanel panelBouton = new JPanel();
						JButton boutonClose = new JButton("Fermer");
						panelBouton.add(boutonClose);
						panel.add(panelBouton);
						
						// Action du bouton retour
						boutonClose.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent arg0) {
								// Ferme l'application
								window.dispose();
							}
						});
					} else {
						// Aucune image dans le dossier, on affiche un message d'alerte
						JOptionPane.showMessageDialog(window, "Merci de sélectionner un dossier avec des images.");
					}
				}
			}
		});
		
		// Ajoute le bouton au Panel
		panel.add(boutonChoisir);
		window.setContentPane(panel);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
	}
	
	/**
	 * Crée un dossier pour les nouvelles images et compresse les images
	 * 
	 * @param listFiles
	 */
	public static void resizeFiles(final File[] listFiles){
		// Crée un dossier pour les images redimmensionnées
		compressFolder = new File(rootFolder.getAbsolutePath() + File.separatorChar + "Resize");
		compressFolder.mkdir();

		// Excecution dans un thread
		Runnable runner = new Runnable() {
            public void run() {
            	// Met la barre de progression à 0%
        		progressBar.setValue(0);
        		progressBar.repaint();
        		final int ratio = 100 / listFiles.length;
				for (final File file : listFiles) {
					cpt++;
					String progressLabel = cpt+"/"+listFiles.length+": "+file.getName();
					label2.setText(progressLabel);
                	// Redimensionne l'image
        			compressJPEG(file);
        			// Mise à jour de la progressBar
            		progressBar.setValue(cpt * ratio);
            		progressBar.repaint();
                }
            	// Met la barre de progression à 100%
        		progressBar.setValue(100);
        		progressBar.repaint();
			}
        };
        Thread t = new Thread(runner, "Code Executer");
        t.start();
	}
	
	/**
	 * Compresse une image JPEG
	 * 
	 * @param file
	 */
	@SuppressWarnings("resource")
	public static void compressJPEG(File file){
		try {
			File compressedImageFile = new File(compressFolder.getAbsolutePath() + File.separatorChar + file.getName());

			InputStream is = new FileInputStream(file);
			OutputStream os = new FileOutputStream(compressedImageFile);

			float quality = 0.5f;

			// create a BufferedImage as the result of decoding the supplied InputStream
			BufferedImage image = ImageIO.read(is);

			// get all image writers for JPG format
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");

			if (!writers.hasNext())
				throw new IllegalStateException("No writers found");

			ImageWriter writer = (ImageWriter) writers.next();
			ImageOutputStream ios = ImageIO.createImageOutputStream(os);
			writer.setOutput(ios);

			ImageWriteParam param = writer.getDefaultWriteParam();

			// compress to a given quality
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);

			// appends a complete image stream containing a single image and
		    //associated stream and image metadata and thumbnails to the output
			writer.write(null, new IIOImage(image, null, null), param);

			// close all streams
			is.close();
			os.close();
			ios.close();
			writer.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ouvre une boite de dialogie pour le choix du répertoire
	 * 
	 * @param panel
	 * @return
	 */
	public static int openDialog(JPanel panel){
		// File Chooser
		jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		return jFileChooser.showOpenDialog(panel);
	}

}
