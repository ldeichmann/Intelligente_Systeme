library(plotly)

m <- data.matrix(read.csv("/home/cru/Code/Intelligente_Systeme/Intelligente_Systeme_02-java/data1.csv", header = FALSE, sep = ","))

ml <- data.matrix(read.csv("/home/cru/Code/Intelligente_Systeme/Intelligente_Systeme_02-java/rommel.csv", header = FALSE, sep = ","))

x <- seq_len(ncol(m))
y <- seq_len(nrow(m))

x1 <- ml[,1]
y1 <- ml[,2]
z <- matrix()
len <- length(x1)
for(i in 1:len)
{
  z[i] <- m[y1[i]+1,x1[i]+1]
}


p <- plot_ly() %>% add_surface(x = ~x, y = ~y, z = ~m) %>% 
  add_trace(x = ~x1, y = ~y1, z = ~z, mode = "markers", type = "scatter3d", 
            marker = list(size = 5, color = "red", symbol = 104))
htmlwidgets::saveWidget(p, "/home/cru/Documents/test1.html")
