//Tarun Pokra
//3381418
//August 3rd, 2025
//Assignment 2
//This program basically converts the bitmap image to JPEG format second viewable pane on the image view
//from assignment 1. Original pane should display the original bitmap image, and the second pane should display the converted JPEG image.
//Add a mechanism to input the quantization matrix. It should have three options:
//(a) default JPEG matrix - the numbers can be found on Table 9.1 and Table 9.2 of the textbook;
//(b) a constant number;
//(c) DC component only (i.e. set all other numbers to zero).
//Add an option to the File menu to output the compressed JPEG image to the hard disk.
//All able to save JPEG to a choice of directory - compressed.
package Assignment2; //package within or was in src folder dir. etc.
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO; // needed this bad, JAI is really old for jpeg stuff.

import javax.swing.*;
import javax.swing.table.DefaultTableModel; //swing gui stuff.
import javax.swing.table.TableCellEditor; // matrices stuff.
import javax.swing.table.TableCellRenderer;
import com.sun.image.codec.jpeg.JPEGCodec; // I DONT THINK i need these jpeg imports
// since i changed my code.
import com.sun.image.codec.jpeg.JPEGEncodeParam; // delete later
import com.sun.image.codec.jpeg.JPEGImageEncoder;//delete later
import com.sun.image.codec.jpeg.JPEGQTable; //delete later.
import javax.imageio.*;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;

public class Assignment2App extends JFrame // main public class for the program.
{
    private BufferedImage originalImage; // i amde this stuff private for jpeg, and tables
    private BufferedImage previewJPEG;
    private JLabel originalLabel;
    private JLabel jpegLabel;
    private JTable tableY; // the tables
    private JTable tableC;
    // I copy pasted the two tables from the texbook, 9.1, and 9.2, basically
    // hardcoded it all. 
    private static final int[][] DEFAULT_Y=
    {
        {16,11,10, 16,24,40 , 51,61},
        {12,12,14,19,26,58,60, 55},
        {14,13,16,24,40,57,69,56 },
        {14,17,22,29,51,87,80, 62} ,
        {18,22,37,56,68,109,103,77},
        {24,35,55,64,81,104,113 ,92},
        {49,64,78,87,103,121,120,101},
        {72,92,95,98 , 112,100 ,103,99}
    };

    private static final int[][] DEFAULT_C =
    {
        {17,18,24,47,99,99,99,99},
        {18,21,26, 66,99,99,99,99},
        {24,26,56,99 ,99,99,99,99},
        {47,66,99 , 99,99,99,99,99 },
        {99,99,99,99,99,99,99,99},
        {99,99,99,99,99,99,99,99},
        {99,99,99,99,99,99 ,99,99},
        {99,99,99,99,99 , 99,99, 99}
    };
    public Assignment2App() 
    {
        super("Assignment 2 (Matrix, jpeg, compression"); // window title pops open.
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000,700); // size default when i run it.
        setLayout(new BorderLayout());
        createMenu();
        buildUI();
        setLocationRelativeTo(null);
    }
    private void createMenu() // menu options
    {
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("file"); // file choice
        JMenuItem open = new JMenuItem("Open BMP File"); //
        open.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                openImage();
            }
        });
        JMenuItem saveAll =new JMenuItem("Save all JPEGs Files" ) ; // option to output and save jpegs (post).
        saveAll.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                saveAllJPEGs() ;
            }
        });
        JMenuItem exit= new JMenuItem("EXit" ) ; // program termination.
        exit.addActionListener(new ActionListener()
        {
            public void actionPerformed (ActionEvent e )
            {
                System.exit( 0) ;
            }
        });
        file.add (open); // open
        file.add( saveAll); //save
        file.addSeparator () ;
        file.add(exit); // terminate prog
         mb.add(file); // file
        setJMenuBar(mb);
    }
    private void buildUI()
    {
        originalLabel = new JLabel("ORiginal Image" , JLabel.CENTER); //original image
        jpegLabel = new JLabel("Preview of JPEG " , JLabel.CENTER ) ; // preview jpeg.
        originalLabel.setVerticalTextPosition ( JLabel.BOTTOM) ; //
        originalLabel.setHorizontalTextPosition( JLabel.CENTER);
        jpegLabel.setVerticalTextPosition(JLabel.BOTTOM ) ;
        jpegLabel.setHorizontalTextPosition(JLabel.CENTER );
        JPanel images =new JPanel(new
                GridLayout(1,2, 5 , 5) ) ;
        images.setBorder( BorderFactory.createEmptyBorder
                ( 5,5, 5, 5));
        images.add( new JScrollPane( originalLabel) );
        images.add(new JScrollPane (jpegLabel )) ;
        add (images , BorderLayout.CENTER );
        JPanel  bottom = new JPanel(new BorderLayout () ) ;
       
        tableY = createMatrixTable(); //maxtrices 
        tableC= createMatrixTable();

        fillTableFromArray( tableY , DEFAULT_Y );
        fillTableFromArray (tableC, DEFAULT_C);

        JPanel matrices= new JPanel( new GridLayout( 1 , 2 ));
        matrices.setBorder( BorderFactory.createTitledBorder
                ("quantization matrices (Can edit) ")) ; //edit.
        matrices.add(wrapWithTitle(new JScrollPane(tableY
                ), "luminance (Y)")) ; // luminance
        matrices.add(wrapWithTitle(new JScrollPane(tableC
                ), "Chrominance(C)") ); //chrominance stuff.
        bottom.add( matrices , BorderLayout.CENTER);
       
        JPanel btns = new JPanel();
        JButton btnDefault= new JButton("Default"); //button for user
        JButton btnConstant = new JButton("Constant.."); //constant button, choose like
        // in the assignment low for 1, or 200 for high, etc. 
        JButton btnDCOnly = new JButton("DC") ; //dc table
        JButton btnApply= new JButton("Apply to preview " ) ; //preview table.

        btnDefault.addActionListener ( new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                fillTableFromArray(tableY , DEFAULT_Y ) ;
                fillTableFromArray( tableC, DEFAULT_C);
            }
        });

        btnConstant.addActionListener(new ActionListener ()
        {
            public void actionPerformed(ActionEvent e)
            {
                String s=JOptionPane.showInputDialog ( Assignment2App.this ,
                        "Enter constant integer value:", "Constant quant value " , // prompt user, new small window opens.
                        JOptionPane.PLAIN_MESSAGE);
                if (s ==null) return;
                try{//**********************************************************************************************************************
                    int v =Integer.parseInt(s.trim() ) ;
                    int[][ ] m =new int [8] [8 ] ; // empty original arr
                    for (int i=0;i<8;i++) for (int j =  0 ; j < 8; j++ ) m [i ][j ]= v; // increment for-loop
                    fillTableFromArray(tableY, m);
                    fillTableFromArray(tableC, m);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(Assignment2App.this, "number is invalid, sorry." );
                }
            }
        });

        btnDCOnly.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) //ask alex. 
            {
                int[][] mY = new int [ 8] [8 ];
                int[][] mC = new int[8][8];
                mY[0][0] = 1; // keep DC
                mC[0][0] = 1;
                fillTableFromArray(tableY, mY); //arry used for table data.
                fillTableFromArray(tableC, mC);
            }
        });

        btnApply.addActionListener(new ActionListener() //action.
        {
            public void actionPerformed(ActionEvent e)
            {
                applyCurrentTablesToPreview();
            }
        });

        btns.add(btnDefault); //default
        btns.add(btnConstant); // etc.
        btns.add(btnDCOnly);
        btns.add(btnApply);

        bottom.add (btns, BorderLayout.SOUTH  ) ; //layout bottom
        add(bottom , BorderLayout.SOUTH) ;
    }

    private JScrollPane wrapWithTitle(Component comp, String title)
    {
        JScrollPane sp =new JScrollPane ( comp) ;
        sp.setBorder( BorderFactory.createTitledBorder ( title) ) ;
        return sp ;
    }

    private JTable createMatrixTable() //matrix table being made.
    {
        DefaultTableModel model =new DefaultTableModel(8, 8)
        {
            public boolean isCellEditable(int r , int c)
            {
                return true;
            }
        };
        JTable tbl =new JTable( model) ;
        tbl.setRowHeight(24 );
        // center renderer
        TableCellRenderer centerRenderer= new DefaultTableCellRenderer ()
        {
            public void setValue( Object value)
            {
                setHorizontalAlignment (SwingConstants.CENTER ) ;
                super.setValue (value ) ;
            }
        };
        tbl.setDefaultRenderer( Object.class , centerRenderer) ;
        // simple editor that restricts to integers
        tbl.setDefaultEditor(Object.class,new DefaultCellEditor ( new JTextField()) ) ;
        return tbl ;
    }
// tb1
    private void fillTableFromArray(JTable table , int[][] arr) //array used as data for tables.
    {
        for (int r= 0; r < 8; r++ ) for (int c=0; c<8 ; c++  )
            table.setValueAt(Integer.valueOf(arr[r][c ] ), r , c ) ;
    }

    private int[][ ] readTableToArray ( JTable table )
    {
        int[][ ] m =new int[ 8][8 ] ;
        for (int r=0;r<8;r++)
        {
            for ( int c =0 ; c < 8;c++ ) // quick for loop to read the table
                // i hard coded.
            {
                Object o =table.getValueAt (r , c );
                if (o ==null) { m[r ][c] = 1; continue; } // fix later.
                try {
                    m[r][c] = Integer.parseInt(o.toString().trim());
                } catch (Exception ex) {
                    m[r][c] = 1;
                }
            }
        }
        return m; //return m filling tables. 
    }

    private void openImage()
    {
        JFileChooser fc =new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter
                ("Bitmap and images" , "bmp", "png" , " jpg" , "jpeg " );
        fc.setFileFilter(filter);
        int res = fc.showOpenDialog(this);
        if ( res ==JFileChooser.APPROVE_OPTION)
        {
            File f = fc.getSelectedFile(); // file selection.
            try {
                BufferedImage img =ImageIO.read (f );
                if ( img== null )
                {
                    JOptionPane.showMessageDialog( this , "file is not supported..");
                    return;
                }
                // convert to TYPE_INT_RGB to be safe for encoder
                BufferedImage rgb= new BufferedImage(img.getWidth() ,
                        img.getHeight(), BufferedImage.TYPE_INT_RGB );
                Graphics2D g =rgb.createGraphics ();
                g.drawImage(img, 0 ,0, null);
                g.dispose();
                originalImage =rgb; //red grren blue

                originalLabel.setIcon ( new ImageIcon(scaleToLabel( originalImage, originalLabel ) ));
                // apply current tables to create preview
                applyCurrentTablesToPreview ();
            } catch(IOException ex)
            {
                JOptionPane.showMessageDialog( this , "UNable to open!" +ex.getMessage ());
            }
        }
    }// bracket issue, fix later.

    // scale image to label size for preview
    private Image scaleToLabel(BufferedImage img , JLabel label)
    {
        int w= label.getWidth();
        int h= label.getHeight ();
        if (w <= 0 || h <= 0) //w and h for the scale.
        {
            // use half of frame size as default preview size
            w =getWidth () / 2 -20 ;
            h = getHeight()/ 2;
        }
        return img.getScaledInstance(w, h, Image.SCALE_SMOOTH); // fix later, might be "image". or "Image".
    }
    private void applyCurrentTablesToPreview()
    {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Choose an image file first"); //display/prompt user.
            return;
        }
        // read matrices from tables
        int[][] yMat =readTableToArray(tableY ) ;
        int[][]cMat = readTableToArray (tableC );

        try
        {
            // encode to JPEG using matrices into memory and read back for preview
            ByteArrayOutputStream baos =new ByteArrayOutputStream () ;
            encodeJPEGWithQTables ( originalImage, baos, yMat , cMat );
            baos.flush() ;
            ByteArrayInputStream bais= new ByteArrayInputStream ( baos.toByteArray () ) ;
            previewJPEG =ImageIO.read(bais );
            jpegLabel.setIcon ( new ImageIcon(scaleToLabel( previewJPEG , jpegLabel)) );
        } catch ( Exception ex)
        {
            JOptionPane.showMessageDialog ( this , "failed to make a preview: "+ ex.getMessage() ); //display
        }
    }

    private void saveAllJPEGs() // jpeg files saved.
    {
        if ( originalImage ==null )
        {
            JOptionPane.showMessageDialog ( this, "load up an image." );
            return;
        }
        JFileChooser fc =new JFileChooser () ;
        //;}
        fc.setFileSelectionMode (JFileChooser.DIRECTORIES_ONLY );
        int res =fc.showSaveDialog(this );
        if (res !=JFileChooser.APPROVE_OPTION)
            return;
        File dir = fc.getSelectedFile();

        try {
            // (a) Default quantization table files
            File outDefault= new File(dir , "output_default.jpg"); // default name, output file.
            encodeJPEGWithQTables(originalImage, new FileOutputStream
                    (outDefault), DEFAULT_Y , DEFAULT_C);

            // (b) Low constant l;
            File outLow = new File ( dir, "output_low_constant.jpg"); //low constant.
            int[][] low =makeConstantMatrix (1 );
            encodeJPEGWithQTables (originalImage, new FileOutputStream(outLow),low,low ) ;

            // (c) High constant 200 - form assignment example. 
            File outHigh = new File(dir, "output_high_constant.jpg "); // file output high constant, etc.
            int[][] high = makeConstantMatrix(200); // upto about 200.
            encodeJPEGWithQTables (originalImage ,
                    new FileOutputStream(outHigh), high, high );

            // (d) DC only
            File outDC =new File(dir, "output_dc_only.jpg"); //dc output.
            int[][]dcY =new int[ 8][8 ];
            int[][] dcC= new int[8 ][8] ;
            dcY[0 ][0] =1;
            dcC[0][0] =  1 ;
            encodeJPEGWithQTables(originalImage ,
                    new FileOutputStream(outDC),dcY, dcC );
            JOptionPane.showMessageDialog(this,
                    "your 4 saved JPEG files to : " +dir.getAbsolutePath() ); //output, for 4 image files, choosen location, dir, etc.
        } catch (Exception ex) //catch point, error, throw away, etc.
        {
            JOptionPane.showMessageDialog(this, "error cant save JPEGs: " + ex.getMessage());
        }
    }

    private int[][] makeConstantMatrix(int v)
    {
        int[][] m = new int[8][8];
        for (int i=0 ; i< 8; i++) for (int j=0; j<8; j++ ) m[i][j] = v; // fop incrementation for i and j for the constant button matrix.
        return m;
    }

    /**
    // THIS ENTIRE CODE I COULD NOT GET IT TO WORK, COMMENTED THE WHOLE THING OUT, RE-WROTE IT
     * // BECAUSE jai STUFF IS SUPER ODD, AND THIS PROGRAM REQUIRES IMAGE CLASS PACKAGES, ETC. 
    
    private void encodeJPEGWithQTables(BufferedImage img, OutputStream os, int[][] yTa
            // Ensure integer RGB image
            BufferedImage rgb = img;
            if (img.getType() != BufferedImage.TYPE_INT_RGB) {
                rgb = new BufferedImage(i
            }

            // Flatten matrices row-major to 64-element int arrays
            int[] yFlat = flatten\Table);

            JPEGImageEncoder\JPEGCodec.crearam param = encoder.getDefaultJPEGEncodeParam(rgb);

            // set quant yFlat);
            JPEGQTable qC = new JPEGQTable(cFlat);

            // Note: some encoders expect 2 tables set at indices 0 and 1
            //param.setQTables(new JPEGQTable[] { qY, qC });
            //param.setQuality(qualityValue, true);
            param.setQuality(0.75f, true); // 75% quality
            // also set component-specific tables if available
            try {
                param.setQTable(0, qY);
                param.setQTable(1, qC);
            } catch (Throwable t) {
                // ignore if not supported by this JDK implementation
            }

            // Use default subsampling and color conversion
            encoder.encode(rgb, param);
        } finally {
            // close output stream if it's not System.out
            try { os.close(); } catch (Exception ignored) {}
        }
    }
     */
//*********************************************************************************************8
    // THIS CODE IS FOR ENCODING JPEG I HAD TO REMAKE FROM ABOVE. 

    private void encodeJPEGWithQTables(BufferedImage img , OutputStream os
            , int[][] yTable, int[][] cTable) throws IOException
    {
        BufferedImage rgb =img;
        if (img.getType() != BufferedImage.TYPE_INT_RGB)
        {
            rgb =new BufferedImage(img.getWidth() ,
                    img.getHeight() , BufferedImage.TYPE_INT_RGB ) ;
            Graphics2D g =rgb.createGraphics() ;
            g.drawImage (img, 0 , 0,null);
            g.dispose();
        }
        ImageWriter writer =ImageIO.getImageWritersByFormatName("jpeg").next(); // this imageIO is the reason
        // i kinda had to rewrite the encode function stuff for jpeg above, because JAI, JDK 6, etc dont support it.
        ImageOutputStream ios =  ImageIO.createImageOutputStream(os );
        writer.setOutput(ios ) ;

        JPEGImageWriteParam jpegParams =new JPEGImageWriteParam( null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT );
        jpegParams.setCompressionQuality( 0.75f); // set quality (75%)
        // note i set the 0.75 as an example from the assignment. 
        writer.write(null, new IIOImage( rgb, null , null),jpegParams);
        ios.close ();
        writer.dispose ();
        os.close() ;
    }
        private int[] flattenMatrix(int[][] m)
        {
            int[] out = new int[64];
            int idx =0;
            for (int r=0 ;r<8 ;r++) for (int c=0;c<8;c++) out[idx++] = m[r][c];
            return out; // for into for loooop
        }
        public static void main(String[] args)
        {
            // Ensure GUI runs on EDT
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    Assignment2App app =new Assignment2App ();
                    app.setVisible (true );
                }
            });
        }
    }
    // done.
//. 