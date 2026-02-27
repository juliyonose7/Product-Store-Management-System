export interface SalesSummary {
  totalRevenueMonth: number;
  totalSalesMonth: number;
  totalQuantityMonth: number;
}

export interface DailySalesMetric {
  saleDate: string;
  totalRevenue: number;
  totalQuantity: number;
  totalSales: number;
}

export interface TopProductMetric {
  productId: number;
  productName: string;
  totalQuantity: number;
  totalRevenue: number;
}

export interface DashboardMetrics {
  summary: SalesSummary;
  dailySales: DailySalesMetric[];
  topProducts: TopProductMetric[];
}
