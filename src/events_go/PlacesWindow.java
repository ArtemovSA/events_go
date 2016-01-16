/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package events_go;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

/**
 *
 * @author Sergey
 */
public class PlacesWindow extends javax.swing.JFrame {

    private String driver_db;
    private String url_db;
    private String login_db;
    private String pass_db;
    
    int states = 0;
    int add_state = 0;
    int ID_add_place = 0;
    String ID_add_event = "";
    int format = 3;
    
    private static final String URL         = "https://maps.googleapis.com/maps/api/geocode/xml";
    private static final String DEFAULT_KEY = "AIzaSyBTvVs2Lcn_Ul9NjmSDFlxw3CbYhInszxI"; 
    
    JMapViewer map;
    
     class coordinateClass { 
        double lat;
        double lon;
     }

    String[] head_place = {"ID_PLACE","NAME","ADDRESS"};
    String[] head_object = {"ID_OBJECT","NAME","TYPE","LAT","LON","AREA"};
    DefaultTableModel tableModelPlace = new DefaultTableModel(null, head_place);
    DefaultTableModel tableModelObject = new DefaultTableModel(null, head_object);
    
    class PlaceClass {
        int ID;
        String name;
        String Address;
    }
    
    /**
     * Creates new form PlacesWindow
     */
    public PlacesWindow() {
        initComponents();
        jButton_ok.setVisible(false);
        jButton_cancel.setVisible(false);
    }
    
     private void setMapPoint(coordinateClass coord) {
        map.removeAllMapMarkers();
        map.addMapMarker(new MapMarkerDot(coord.lat, coord.lon));
    }
    
    private void setMapCenter(coordinateClass coord, int zoom) {
        ICoordinate center = map.getPosition();
        center.setLat(coord.lat);
        center.setLon(coord.lon);
        map.setDisplayPosition(center, zoom);
    }
    
    public void GetObjects(int row_n) {
        
        coordinateClass coord = getCoordinate(tableModelPlace.getValueAt(row_n, 2).toString());
        setMapPoint(coord);
        setMapCenter(coord, 17);
        
        jTextField_name.setText(tableModelPlace.getValueAt(row_n, 1).toString());
        jTextField_address.setText(tableModelPlace.getValueAt(row_n, 2).toString());
        try {
            
            int counter = 0;
            tableModelObject.setRowCount(0);
            
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment

            //Builds
            String query_str = "SELECT BM.ID_OBJECT,BM.type,BM.name, SDO_GEOM.SDO_CENTROID(BM.shape, m.diminfo).sdo_point.x as LAT, SDO_GEOM.SDO_CENTROID(BM.shape, m.diminfo).sdo_point.y as LON, SDO_GEOM.SDO_AREA(BM.shape, 0.005)*1000000000 as area" 
                            +" FROM user_sdo_geom_metadata m, GEO_OBJECT BM" 
                            +" INNER JOIN GEO_PLACE_OBJECT PB ON BM.ID_OBJECT = PB.ID_OBJECT"
                            +" INNER JOIN GEO_PLACE PL ON  PB.ID_PLACE = PL.ID_PLACE"
                            +" WHERE (m.table_name = 'GEO_BUILD_MAP' AND m.column_name = 'SHAPE') and (PL.ID_PLACE='" + tableModelPlace.getValueAt(row_n, 0) + "')"; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                String ID = String.valueOf(query_result.getInt("ID_OBJECT"));
                String name = query_result.getString("NAME");
                String type = query_result.getString("TYPE");
                String lat = String.valueOf(query_result.getFloat("LAT"));
                String lon = String.valueOf(query_result.getFloat("LON"));
                String area = String.valueOf(query_result.getFloat("AREA"));
                String[] row = {ID,name,type,lat,lon,area};
                
                tableModelObject.addRow(row);
                counter++;
            }
            
            
            map.removeAllMapMarkers();
            
            for (int i=0; i<counter; i++) {
                Float lat = Float.valueOf(tableModelObject.getValueAt(i, 3).toString());
                Float lon = Float.valueOf(tableModelObject.getValueAt(i, 4).toString());
                map.addMapMarker(new MapMarkerDot(lon, lat));
            }
            
                    
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private void loadPlaces() {
        try {
            
        int counter = 0;
        String query_str = "SELECT * FROM GEO_PLACE";
        tableModelPlace.setRowCount(0);
        
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment
            if ((format == 3)||(format == 2)) {
                query_str = "SELECT PL.ID_PLACE, PL.NAME, PL.ADDRESS"
                        + " FROM GEO_PLACE PL"
                        + " INNER JOIN GEO_PLACE_EVENT PE ON PL.ID_PLACE = PE.ID_PLACE"
                        + " WHERE ID_EVENT = '"+ID_add_event+"'"; //Create query
            }
            if (format == 1) {
                query_str = "SELECT *"
                        + " FROM GEO_PLACE"; //Create query
            }
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                String ID = String.valueOf(query_result.getInt("ID_PLACE"));
                String name = query_result.getString("NAME");
                String address = query_result.getString("ADDRESS");
                String[] row = {ID,name,address};
                tableModelPlace.addRow(row);
                counter++;
            }
            
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void addObject(ICoordinate coordinate, int ID_place) {
        try {
         
            String ID_object = "0";
                    
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment
            String query_str =  "SELECT ID_OBJECT" +
                                " FROM GEO_OBJECT GO " +
                                " WHERE SDO_NN (GO.shape, " +
                                                " mdsys.sdo_geometry(2001," +
                                                " NULL," +
                                                " mdsys.sdo_point_type("+ coordinate.getLon()+","+coordinate.getLat()+",0)," +
                                                " NULL," +
                                                " NULL)," +
                                                " 'sdo_num_res=1') = 'TRUE'"; //Create query
            
            ResultSet query_result = stmt.executeQuery(query_str); //Get result
            
            while(query_result.next()){
                ID_object = String.valueOf(query_result.getInt("ID_OBJECT"));
            }
            
            query_str =  "INSERT INTO GEO_PLACE_OBJECT (ID_PLACE,ID_OBJECT)"+
                    " VALUES ('"+String.valueOf(ID_place)+"','"+String.valueOf(ID_object)+"')";
            
            stmt.executeQuery(query_str);
            
            db_connect.close();
            stmt.close();
            query_result.close();
            
        } catch (SQLException ex) {
            Logger.getLogger(load_shape.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel_map = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable_obj = new javax.swing.JTable();
        jButton_addObject = new javax.swing.JButton();
        jButton_delObject = new javax.swing.JButton();
        jTextField_name = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jTextField_address = new javax.swing.JTextField();
        jButton_ok = new javax.swing.JButton();
        jButton_cancel = new javax.swing.JButton();
        jButton_add = new javax.swing.JButton();
        jButton_update = new javax.swing.JButton();
        jButton_del = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable_place = new javax.swing.JTable();
        jButton_Place = new javax.swing.JButton();
        jButton_Cancel_Place = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jPanel_map.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout jPanel_mapLayout = new javax.swing.GroupLayout(jPanel_map);
        jPanel_map.setLayout(jPanel_mapLayout);
        jPanel_mapLayout.setHorizontalGroup(
            jPanel_mapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 459, Short.MAX_VALUE)
        );
        jPanel_mapLayout.setVerticalGroup(
            jPanel_mapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jTable_obj.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane3.setViewportView(jTable_obj);

        jButton_addObject.setText("Add object");
        jButton_addObject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_addObjectActionPerformed(evt);
            }
        });

        jButton_delObject.setText("Del object");
        jButton_delObject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_delObjectActionPerformed(evt);
            }
        });

        jLabel1.setText("Name");

        jLabel2.setText("Address");

        jButton_ok.setText("OK");
        jButton_ok.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_okActionPerformed(evt);
            }
        });

        jButton_cancel.setText("Cancel");
        jButton_cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_cancelActionPerformed(evt);
            }
        });

        jButton_add.setText("Add");
        jButton_add.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_addActionPerformed(evt);
            }
        });

        jButton_update.setText("Update");
        jButton_update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_updateActionPerformed(evt);
            }
        });

        jButton_del.setText("Delete");
        jButton_del.setMaximumSize(new java.awt.Dimension(67, 23));
        jButton_del.setMinimumSize(new java.awt.Dimension(67, 23));
        jButton_del.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_delActionPerformed(evt);
            }
        });

        jTable_place.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jTable_place.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable_placeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable_place);

        jButton_Place.setText("Add Place");
        jButton_Place.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_PlaceActionPerformed(evt);
            }
        });

        jButton_Cancel_Place.setText("Cancel");
        jButton_Cancel_Place.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton_Cancel_PlaceActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addComponent(jLabel1))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jLabel2)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField_name)
                            .addComponent(jTextField_address)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton_ok, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_cancel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_add, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_update)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton_del, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 357, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jButton_Place)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_Cancel_Place)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton_delObject)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton_addObject))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton_addObject)
                            .addComponent(jButton_delObject)
                            .addComponent(jButton_Cancel_Place)
                            .addComponent(jButton_Place)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField_name, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextField_address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton_ok)
                            .addComponent(jButton_cancel)
                            .addComponent(jButton_add)
                            .addComponent(jButton_update)
                            .addComponent(jButton_del, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel_map, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel_map, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton_addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addActionPerformed
        jButton_ok.setVisible(true);
        jButton_cancel.setVisible(true);
        jTextField_name.setText("");
        jTextField_address.setText("");
        jTextField_name.requestFocus();
        states = 1;
    }//GEN-LAST:event_jButton_addActionPerformed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        map = new JMapViewer();
        map.setTileSource(new OsmTileSource.Mapnik());
        map.setTileLoader(new OsmTileLoader(map));
        map.setTileGridVisible(map.isTileGridVisible());
        ICoordinate center = map.getPosition();
        center.setLat(51.8311104);
        center.setLon(12.2429261);
        map.setDisplayPosition(center, 10);
        
        jPanel_map.setLayout(new BorderLayout());
        jPanel_map.add(map, BorderLayout.CENTER);
        jPanel_map.revalidate();
        
        new DefaultMapController(map){
            @Override
            public void mouseClicked(MouseEvent e) {
                if (add_state == 1) {
                    addObject(map.getPosition(e.getPoint()),ID_add_place);
                    add_state = 0;
                    //jTable_place.setRowSelectionInterval(0, 0);
                    GetObjects(jTable_place.getSelectedRow()); 
                }
                if ((states == 1)||(states == 2)) {
                    coordinateClass coord = new coordinateClass();
                    coord.lat = map.getPosition(e.getPoint()).getLat();
                    coord.lon = map.getPosition(e.getPoint()).getLon();
                    String address = getAddress(coord);                 
                    jTextField_address.setText(address);
                }
                System.out.println(map.getPosition(e.getPoint()));
            }
        };
        
        jTable_place.setModel(tableModelPlace);
        jTable_obj.setModel(tableModelObject);
        loadPlaces();  
        if (jTable_place.getRowCount() > 0) {
            jTable_place.setRowSelectionInterval(0, 0);
            GetObjects(0);  
        }
    }//GEN-LAST:event_formWindowOpened

    private void jButton_okActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_okActionPerformed
        PlaceClass Place = new PlaceClass();
        Place.name = jTextField_name.getText();
        Place.Address = jTextField_address.getText();
        if (states == 1) {
            insertInDB(Place);
        }
        if (states == 2) {
            Place.ID = Integer.parseInt(tableModelPlace.getValueAt(jTable_place.getSelectedRow(), 0).toString());
            updateDB(Place);
        }   
        if (states == 3) {
            deleteDB(Integer.parseInt(tableModelPlace.getValueAt(jTable_place.getSelectedRow(), 0).toString()));
        } 
        loadPlaces();
        jTable_place.setRowSelectionInterval(0, 0);
        GetObjects(0);
        jButton_ok.setVisible(false);
        jButton_cancel.setVisible(false);
        states = 0;
    }//GEN-LAST:event_jButton_okActionPerformed

    private void jButton_cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_cancelActionPerformed
        jButton_ok.setVisible(false);
        jButton_cancel.setVisible(false);
    }//GEN-LAST:event_jButton_cancelActionPerformed

    private void jTable_placeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable_placeMouseClicked
        GetObjects(jTable_place.getSelectedRow());
    }//GEN-LAST:event_jTable_placeMouseClicked

    private void jButton_updateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_updateActionPerformed
        jButton_ok.setVisible(true);
        jButton_cancel.setVisible(true);
        jTextField_name.requestFocus();
        states = 2;
    }//GEN-LAST:event_jButton_updateActionPerformed

    private void jButton_delActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_delActionPerformed
        jButton_ok.setVisible(true);
        jButton_cancel.setVisible(true);
        states = 3;
    }//GEN-LAST:event_jButton_delActionPerformed

    private void jButton_addObjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_addObjectActionPerformed
        add_state = 1;
        ID_add_place = Integer.parseInt(jTable_place.getValueAt(jTable_place.getSelectedRow(),0).toString());
    }//GEN-LAST:event_jButton_addObjectActionPerformed

    private void jButton_delObjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_delObjectActionPerformed
        DeleteObject(Integer.parseInt(jTable_obj.getValueAt(jTable_obj.getSelectedRow(),0).toString()));
        jTable_place.setRowSelectionInterval(0, 0);
        GetObjects(0);
    }//GEN-LAST:event_jButton_delObjectActionPerformed

    private void jButton_PlaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_PlaceActionPerformed
        if (format == 1) {
            addPlaceToEvent(ID_add_event, jTable_place.getValueAt(jTable_place.getSelectedRow(),0).toString());
            dispose();
        }
        if (format == 2) {
            deletePlaceToEvent(ID_add_event, jTable_place.getValueAt(jTable_place.getSelectedRow(),0).toString());
            dispose();
        }
    }//GEN-LAST:event_jButton_PlaceActionPerformed

    private void jButton_Cancel_PlaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton_Cancel_PlaceActionPerformed
        dispose();
    }//GEN-LAST:event_jButton_Cancel_PlaceActionPerformed

    private coordinateClass getCoordinate(String addres_str) {
        coordinateClass coord = new coordinateClass();
        
        try {
            String url = (URL + "?address=" + addres_str + "&key=" + DEFAULT_KEY);
            
            Document doc = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36")
                    .timeout(20000)
                    .get();
            
            Elements links_lat = doc.select("location lat");
            Elements links_lon = doc.select("location lng");
            
            coord.lat = Float.parseFloat(links_lat.get(0).text());
            coord.lon = Double.parseDouble(links_lon.get(0).text());
            
            return coord;
        } catch (IOException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
            coord.lat = 51.8311104;
            coord.lon = 12.2429261;   
            return coord;
        }
    }
    
    private String getAddress(coordinateClass coord) {
        String address = "";
        try {
            String url = (URL + "?latlng=" + String.valueOf(coord.lat)+","+ String.valueOf(coord.lon) + "&key=" + DEFAULT_KEY);
            
            Document doc = Jsoup
                    .connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36")
                    .timeout(20000)
                    .get();
            
            Elements links = doc.select("formatted_address");     
            
            address = links.get(0).text();

        } catch (IOException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }
    
    public void Set_format (int format_in) {
        format = format_in;
        if (format_in == 1) {
            jButton_add.setVisible(false);
            jButton_update.setVisible(false);
            jButton_del.setVisible(false);   
            jButton_Place.setVisible(true);
            jButton_Cancel_Place.setVisible(true);
            jButton_Place.setText("Add Place");
        }
        if (format_in == 2) {
            jButton_add.setVisible(false);
            jButton_update.setVisible(false);
            jButton_del.setVisible(false); 
            jButton_Place.setVisible(true);
            jButton_Cancel_Place.setVisible(true);
            jButton_Place.setText("Delete Place");
        }
        if (format_in == 3) {
            jButton_add.setVisible(false);
            jButton_update.setVisible(false);
            jButton_del.setVisible(false);      
            jButton_Place.setVisible(false);
            jButton_Cancel_Place.setVisible(false);
        }  
        if (format_in == 4) {    
            jButton_Place.setVisible(false);
            jButton_Cancel_Place.setVisible(false);
        } 
    }
    
    private void addPlaceToEvent(String ID_Event, String ID_Place) {
        try {
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment

            String query_str = "INSERT INTO GEO_PLACE_EVENT (ID_EVENT,ID_PLACE) VALUES ('"+ID_Event+"','" + ID_Place + "')";
            stmt.executeQuery(query_str);
           
            db_connect.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
     private void deletePlaceToEvent(String ID_Event, String ID_Place) {
        try {
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment

            String query_str = "DELETE FROM GEO_PLACE_EVENT WHERE ID_EVENT = '"+ID_Event+"' and ID_PLACE ='"+ ID_Place + "'";
            stmt.executeQuery(query_str);
           
            db_connect.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    public void Place_send_event(String ID_Event) {
        ID_add_event = ID_Event;
    }
            
    private void DeleteObject(int ID_del) {
        try {
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment

            String query_str = "DELETE FROM GEO_PLACE_OBJECT WHERE ID_OBJECT='"+String.valueOf(ID_del)+"'";
            stmt.execute(query_str);
           
            db_connect.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    private void insertInDB(PlaceClass Place) {
        try {
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment

            String query_str = "INSERT INTO GEO_PLACE (ID_PLACE,NAME,ADDRESS) VALUES ('"+
                                String.valueOf(Place.ID) + "','" + Place.name + "','" + Place.Address + "')";
            stmt.executeQuery(query_str);
           
            db_connect.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void updateDB(PlaceClass Place) {
        try {
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment

            String query_str = "UPDATE GEO_PLACE SET NAME ='"+Place.name+"',ADDRESS='"+Place.Address+"' WHERE ID_PLACE='"+String.valueOf(Place.ID)+"'";
            stmt.execute(query_str);
           
            db_connect.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void deleteDB(int ID_del) {
        try {
            Connection db_connect = DriverManager.getConnection(url_db,login_db,pass_db);
            Statement stmt = db_connect.createStatement(); //Create statment

            String query_str = "DELETE FROM GEO_PLACE_OBJECT WHERE ID_PLACE='"+String.valueOf(ID_del)+"'";
            stmt.execute(query_str);
 
            query_str = "DELETE FROM GEO_PLACE WHERE ID_PLACE='"+String.valueOf(ID_del)+"'";
            stmt.execute(query_str);
           
            db_connect.close();
            stmt.close();
        } catch (SQLException ex) {
            Logger.getLogger(personeClass.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PlacesWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PlacesWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PlacesWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PlacesWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PlacesWindow().setVisible(true);
            }
        });
    }

    public void setConn_params (String driver,String url,String login,String pass) {
        driver_db = driver;
        url_db = url;
        login_db = login;
        pass_db = pass;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton_Cancel_Place;
    private javax.swing.JButton jButton_Place;
    private javax.swing.JButton jButton_add;
    private javax.swing.JButton jButton_addObject;
    private javax.swing.JButton jButton_cancel;
    private javax.swing.JButton jButton_del;
    private javax.swing.JButton jButton_delObject;
    private javax.swing.JButton jButton_ok;
    private javax.swing.JButton jButton_update;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel_map;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable_obj;
    private javax.swing.JTable jTable_place;
    private javax.swing.JTextField jTextField_address;
    private javax.swing.JTextField jTextField_name;
    // End of variables declaration//GEN-END:variables
}
