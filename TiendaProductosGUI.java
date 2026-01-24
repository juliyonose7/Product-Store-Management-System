import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class TiendaProductosGUI extends JFrame {
    // Database connection settings for MySQL
    private static final String URL = "jdbc:mysql://localhost:3306/tienda_productos";
    private static final String USER = "root";
    private static final String PASSWORD = "Farruko1234*";
    
    // Swing UI components
    private JTable tableProductos;
    private JTable tableVentas;
    private DefaultTableModel modelProductos;
    private DefaultTableModel modelVentas;
    private JTextField txtProductoId;
    private JTextField txtCantidad;
    private JTextArea txtResultado;
    private JLabel lblTotalVentas;
    
    public TiendaProductosGUI() {
        setTitle("Sistema de Gestion de Tienda - Base de Datos");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main container with tab navigation
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Tab 1 shows products
        tabbedPane.addTab("Productos", createProductosPanel());
        
        // Tab 2 registers sales
        tabbedPane.addTab("Registrar Venta", createVentasPanel());
        
        // Tab 3 reports sales
        tabbedPane.addTab("Reporte de Ventas", createReportePanel());
        
        // Tab 4 executes SQL routines
        tabbedPane.addTab("Funciones SQL", createFuncionesPanel());
        
        add(tabbedPane);
        
        // Load initial data from database
        cargarProductos();
        cargarVentas();
    }
    
    // Products panel UI
    private JPanel createProductosPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Section title
        JLabel titulo = new JLabel("Lista de Productos Disponibles", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);
        
        // Products table model and view
        String[] columnas = {"ID", "Nombre", "Precio ($)", "Stock"};
        modelProductos = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableProductos = new JTable(modelProductos);
        tableProductos.setFont(new Font("Arial", Font.PLAIN, 14));
        tableProductos.setRowHeight(25);
        tableProductos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(tableProductos);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Actions panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefrescar.addActionListener(e -> cargarProductos());
        buttonPanel.add(btnRefrescar);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // Sales entry panel UI
    private JPanel createVentasPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Section title
        JLabel titulo = new JLabel("Registrar Nueva Venta", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);
        
        // Form layout for sale input
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Datos de la Venta"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Product identifier input
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblProducto = new JLabel("ID del Producto:");
        lblProducto.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblProducto, gbc);
        
        gbc.gridx = 1;
        txtProductoId = new JTextField(15);
        txtProductoId.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtProductoId, gbc);
        
        // Quantity input
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel lblCantidad = new JLabel("Cantidad:");
        lblCantidad.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(lblCantidad, gbc);
        
        gbc.gridx = 1;
        txtCantidad = new JTextField(15);
        txtCantidad.setFont(new Font("Arial", Font.PLAIN, 14));
        formPanel.add(txtCantidad, gbc);
        
        // Form actions
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton btnRegistrar = new JButton("Registrar Venta");
        btnRegistrar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistrar.setBackground(new Color(76, 175, 80));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.addActionListener(e -> registrarVenta());
        buttonPanel.add(btnRegistrar);
        
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.setFont(new Font("Arial", Font.BOLD, 14));
        btnLimpiar.addActionListener(e -> {
            txtProductoId.setText("");
            txtCantidad.setText("");
        });
        buttonPanel.add(btnLimpiar);
        
        formPanel.add(buttonPanel, gbc);
        
        panel.add(formPanel, BorderLayout.NORTH);
        
        // Result output area
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("Resultado"));
        txtResultado = new JTextArea(10, 40);
        txtResultado.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResultado.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtResultado);
        resultPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(resultPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Sales report panel UI
    private JPanel createReportePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Section title
        JLabel titulo = new JLabel("Historial de Ventas", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);
        
        // Sales table model and view
        String[] columnas = {"ID Venta", "Producto", "Cantidad", "Precio Unit.", "Subtotal", "Fecha"};
        modelVentas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableVentas = new JTable(modelVentas);
        tableVentas.setFont(new Font("Arial", Font.PLAIN, 14));
        tableVentas.setRowHeight(25);
        tableVentas.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(tableVentas);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Summary and actions area
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        lblTotalVentas = new JLabel("Total de Ventas: $0.00", SwingConstants.CENTER);
        lblTotalVentas.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalVentas.setForeground(new Color(0, 128, 0));
        bottomPanel.add(lblTotalVentas, BorderLayout.NORTH);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.setFont(new Font("Arial", Font.BOLD, 14));
        btnRefrescar.addActionListener(e -> cargarVentas());
        buttonPanel.add(btnRefrescar);
        
        JButton btnCalcularTotal = new JButton("Calcular Total (Cursor)");
        btnCalcularTotal.setFont(new Font("Arial", Font.BOLD, 14));
        btnCalcularTotal.addActionListener(e -> calcularTotalConCursor());
        buttonPanel.add(btnCalcularTotal);
        
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    // SQL routines panel UI
    private JPanel createFuncionesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Section title
        JLabel titulo = new JLabel("Funciones y Procedimientos SQL", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        panel.add(titulo, BorderLayout.NORTH);
        
        // Options container
        JPanel centerPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Function call to read product stock
        JPanel panelStock = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panelStock.setBorder(BorderFactory.createTitledBorder("Funcion: Obtener Stock de Producto"));
        JLabel lblIdStock = new JLabel("ID Producto:");
        lblIdStock.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField txtIdStock = new JTextField(10);
        txtIdStock.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton btnObtenerStock = new JButton("Consultar Stock");
        btnObtenerStock.setFont(new Font("Arial", Font.BOLD, 14));
        btnObtenerStock.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtIdStock.getText());
                obtenerStockProducto(id);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese un ID valido", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelStock.add(lblIdStock);
        panelStock.add(txtIdStock);
        panelStock.add(btnObtenerStock);
        centerPanel.add(panelStock);
        
        // Procedure call to update stock manually
        JPanel panelActualizar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panelActualizar.setBorder(BorderFactory.createTitledBorder("Procedimiento: Actualizar Stock Manualmente"));
        JLabel lblIdActualizar = new JLabel("ID Producto:");
        lblIdActualizar.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField txtIdActualizar = new JTextField(10);
        txtIdActualizar.setFont(new Font("Arial", Font.PLAIN, 14));
        JLabel lblCantActualizar = new JLabel("Cantidad:");
        lblCantActualizar.setFont(new Font("Arial", Font.BOLD, 14));
        JTextField txtCantActualizar = new JTextField(10);
        txtCantActualizar.setFont(new Font("Arial", Font.PLAIN, 14));
        JButton btnActualizar = new JButton("Actualizar Stock");
        btnActualizar.setFont(new Font("Arial", Font.BOLD, 14));
        btnActualizar.addActionListener(e -> {
            try {
                int id = Integer.parseInt(txtIdActualizar.getText());
                int cantidad = Integer.parseInt(txtCantActualizar.getText());
                actualizarStock(id, cantidad);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Ingrese valores validos", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panelActualizar.add(lblIdActualizar);
        panelActualizar.add(txtIdActualizar);
        panelActualizar.add(lblCantActualizar);
        panelActualizar.add(txtCantActualizar);
        panelActualizar.add(btnActualizar);
        centerPanel.add(panelActualizar);
        
        // Cursor call to calculate total sales
        JPanel panelCursor = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelCursor.setBorder(BorderFactory.createTitledBorder("Cursor: Calcular Total de Todas las Ventas"));
        JButton btnCursor = new JButton("Ejecutar Cursor (Calcular Total Ventas)");
        btnCursor.setFont(new Font("Arial", Font.BOLD, 14));
        btnCursor.setBackground(new Color(33, 150, 243));
        btnCursor.setForeground(Color.WHITE);
        btnCursor.setFocusPainted(false);
        btnCursor.addActionListener(e -> calcularTotalConCursor());
        panelCursor.add(btnCursor);
        centerPanel.add(panelCursor);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Database access methods
    
    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    private void cargarProductos() {
        modelProductos.setRowCount(0);
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM productos ORDER BY id_producto")) {
            
            while (rs.next()) {
                Object[] fila = {
                    rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    String.format("%.2f", rs.getDouble("precio")),
                    rs.getInt("stock")
                };
                modelProductos.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar productos: " + e.getMessage(), 
                "Error de BD", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void cargarVentas() {
        modelVentas.setRowCount(0);
        double total = 0;
        
        String query = "SELECT rv.id_venta, p.nombre, rv.cantidad, p.precio, " +
                      "(rv.cantidad * p.precio) AS subtotal, rv.fecha_venta " +
                      "FROM registro_ventas rv " +
                      "INNER JOIN productos p ON rv.id_producto = p.id_producto " +
                      "ORDER BY rv.fecha_venta DESC";
        
        try (Connection conn = conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                double subtotal = rs.getDouble("subtotal");
                Object[] fila = {
                    rs.getInt("id_venta"),
                    rs.getString("nombre"),
                    rs.getInt("cantidad"),
                    String.format("$%.2f", rs.getDouble("precio")),
                    String.format("$%.2f", subtotal),
                    rs.getDate("fecha_venta")
                };
                modelVentas.addRow(fila);
                total += subtotal;
            }
            
            lblTotalVentas.setText(String.format("Total de Ventas: $%.2f", total));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al cargar ventas: " + e.getMessage(), 
                "Error de BD", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void registrarVenta() {
        try {
            int idProducto = Integer.parseInt(txtProductoId.getText().trim());
            int cantidad = Integer.parseInt(txtCantidad.getText().trim());
            
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, 
                    "La cantidad debe ser mayor a 0", 
                    "Error de Validacion", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Insert sale record. Trigger updates stock
            String query = "INSERT INTO registro_ventas (id_producto, cantidad, fecha_venta) VALUES (?, ?, ?)";
            
            try (Connection conn = conectar();
                 PreparedStatement pstmt = conn.prepareStatement(query)) {
                
                pstmt.setInt(1, idProducto);
                pstmt.setInt(2, cantidad);
                pstmt.setDate(3, Date.valueOf(LocalDate.now()));
                
                pstmt.executeUpdate();
                
                txtResultado.setText("VENTA REGISTRADA EXITOSAMENTE\n\n" +
                                    "Producto ID: " + idProducto + "\n" +
                                    "Cantidad: " + cantidad + "\n" +
                                    "Fecha: " + LocalDate.now() + "\n\n" +
                                    "El trigger 'trg_actualizar_stock' ha actualizado\n" +
                                    "automaticamente el stock del producto.");
                
                // Clear input fields
                txtProductoId.setText("");
                txtCantidad.setText("");
                
                // Reload tables after update
                cargarProductos();
                cargarVentas();
                
                JOptionPane.showMessageDialog(this, 
                    "Venta registrada exitosamente!", 
                    "Exito", 
                    JOptionPane.INFORMATION_MESSAGE);
                
            } catch (SQLException e) {
                txtResultado.setText("ERROR AL REGISTRAR VENTA\n\n" + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Error: " + e.getMessage(), 
                    "Error de BD", 
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Por favor ingrese valores numericos validos", 
                "Error de Validacion", 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void obtenerStockProducto(int idProducto) {
        String query = "{? = CALL obtener_stock_producto(?)}";
        
        try (Connection conn = conectar();
             CallableStatement cstmt = conn.prepareCall(query)) {
            
            cstmt.registerOutParameter(1, Types.INTEGER);
            cstmt.setInt(2, idProducto);
            cstmt.execute();
            
            int stock = cstmt.getInt(1);
            
            if (stock == -1) {
                JOptionPane.showMessageDialog(this, 
                    "El producto con ID " + idProducto + " no existe", 
                    "Producto No Encontrado", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Stock del Producto ID " + idProducto + ": " + stock + " unidades", 
                    "Resultado de la Funcion", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al consultar stock: " + e.getMessage(), 
                "Error de BD", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void actualizarStock(int idProducto, int cantidad) {
        String query = "{CALL actualizar_stock(?, ?)}";
        
        try (Connection conn = conectar();
             CallableStatement cstmt = conn.prepareCall(query)) {
            
            cstmt.setInt(1, idProducto);
            cstmt.setInt(2, cantidad);
            cstmt.execute();
            
            cargarProductos();
            
            JOptionPane.showMessageDialog(this, 
                "Stock actualizado correctamente", 
                "Exito", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al actualizar stock: " + e.getMessage(), 
                "Error de BD", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void calcularTotalConCursor() {
        String query = "{CALL calcular_total_ventas()}";
        
        try (Connection conn = conectar();
             CallableStatement cstmt = conn.prepareCall(query)) {
            
            boolean hasResults = cstmt.execute();
            StringBuilder resultado = new StringBuilder("CURSOR: CALCULO DE TOTAL DE VENTAS\n");
            resultado.append("===================================\n\n");
            
            double total = 0;
            int resultadoNum = 0;
            
            while (hasResults) {
                try (ResultSet rs = cstmt.getResultSet()) {
                    ResultSetMetaData metaData = rs.getMetaData();
                    
                    while (rs.next()) {
                        resultadoNum++;
                        
                        // Append sales detail row
                        if (metaData.getColumnCount() == 1 && 
                            metaData.getColumnName(1).equals("Detalle_Venta")) {
                            resultado.append(rs.getString(1)).append("\n");
                        } else if (metaData.getColumnCount() == 1 && 
                                  metaData.getColumnName(1).equals("Total_Final")) {
                            String totalStr = rs.getString(1);
                            resultado.append("\n").append(totalStr).append("\n");
                            
                            // Parse numeric total value
                            try {
                                String[] parts = totalStr.split("\\$");
                                if (parts.length > 1) {
                                    total = Double.parseDouble(parts[1]);
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
                hasResults = cstmt.getMoreResults();
            }
            
            JOptionPane.showMessageDialog(this, 
                resultado.toString(), 
                "Resultado del Cursor", 
                JOptionPane.INFORMATION_MESSAGE);
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error al ejecutar cursor: " + e.getMessage(), 
                "Error de BD", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String[] args) {
        try {
            // Load MySQL JDBC driver class
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            // Apply system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            TiendaProductosGUI gui = new TiendaProductosGUI();
            gui.setVisible(true);
        });
    }
}