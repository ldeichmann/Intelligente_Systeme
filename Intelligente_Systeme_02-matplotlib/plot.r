library(plotly)

m <- data.matrix(read.csv("/home/cru/Code/Intelligente_Systeme/Intelligente_Systeme_02-java/data0.csv", header = FALSE, sep = ","))

ml <- data.matrix(read.csv("/home/cru/Code/Intelligente_Systeme/Intelligente_Systeme_02-java/cluster.csv", header = FALSE, sep = ","))

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
            marker = list(size = 5, color = "red", symbol = 104)) %>% toWebGL()
htmlwidgets::saveWidget(p, "/home/cru/Documents/cluster0.html")


e <- data.matrix(read.csv("/home/cru/Documents/eval01-3.csv", header = FALSE, sep = ","))
et <- matrix()
lene <- length(e[,1])
for(i in 1:lene)
{
  et[i] <- (2*e[i,9]*e[i,12])/(e[i,9]+e[i,12])
}
max(et)
which(et==max(et))

e_e <- as.data.frame(e)
e_s <- split( e_e , f = e_e$V3 )
e_SELECTED <- e_s$`0.4`[with(e_s$`0.4`, order(V7)), ]
e_t0 <- e_s$`0.2`[with(e_s$`0.2`, order(V7)), ]
e_t1 <- e_s$`0.3`[with(e_s$`0.3`, order(V7)), ]
e_t2 <- e_s$`0.6`[with(e_s$`0.6`, order(V7)), ]
e_t3 <- e_s$`0.7`[with(e_s$`0.7`, order(V7)), ]


p <- plot_ly(data = e_t0, x = ~V7, y = ~V8, type = 'scatter', mode = 'lines', name = 'max_diff = 0.2', line=list(width = 1)) %>%
  add_lines(data = e_SELECTED, x = ~V7, y = ~V8, name = 'max_diff = 0.4 - S', text = ~paste("cluster_dist: ", V6)) %>%
  add_lines(data = e_t1, x = ~V7, y = ~V8, name = 'max_diff = 0.3') %>%
  add_lines(data = e_t2, x = ~V7, y = ~V8, name = 'max_diff = 0.6') %>%
  add_lines(data = e_t3, x = ~V7, y = ~V8, name = 'max_diff = 0.7') %>%
  add_trace(x = e[630,7], y = e[630,8], name = 'max_dist = 99', line = list(color = 'white', size=0), marker = list(size = 5, color = "red"), type = 'scatter', mode = 'lines+markers') %>%
  
  layout(                        
    title = "Recall and Precision for Data 0, Run 3",
    xaxis = list(          
      title = "Recall",      
      showgrid = T),       
    yaxis = list(           
      title = "Precision")
  )
p