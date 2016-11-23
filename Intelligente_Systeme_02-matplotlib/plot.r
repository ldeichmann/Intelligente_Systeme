library(plotly)

m <- data.matrix(read.csv("C:/Users/Adrian/Downloads/data0.csv", header = FALSE, sep = ","))

ml <- data.matrix(read.csv("C:/Users/Adrian/Downloads/label0.csv", header = FALSE, sep = ","))

x <- seq_len(ncol(m))
y <- seq_len(nrow(m))

x1 <- ml[,1]
y1 <- ml[,2]
z <- matrix()
len <- length(x1)
for(i in 1:139)
{
  z[i] <- m[y1[i],x1[i]]
}


plot_ly() %>% add_surface(x = ~x, y = ~y, z = ~m) %>% 
  add_trace(x = ~x1, y = ~y1, z = ~z, mode = "markers", type = "scatter3d", 
            marker = list(size = 5, color = "red", symbol = 104))
