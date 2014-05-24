// depends on bitcoinj 0.11.2
import com.google.bitcoin.core.{ECKey, NetworkParameters, Address}
import com.google.bitcoin.kits.WalletAppKit
import com.google.bitcoin.params.{MainNetParams, RegTestParams, TestNet3Params}
import com.google.bitcoin.utils.BriefLogFormatter
import java.io.File

object Main extends App {
  override def main(args: Array[String]):Unit = {

    BriefLogFormatter.init();

    if (args.length < 2) {
      System.err.println("Usage: address-to-send-back-to [regtest|testnet]");
      return;
    }

    // Figure out which network we should connect to. Each one gets its own set of files.
    val (params, filePrefix): (NetworkParameters, String) = args(1) match{
      case "testnet" => (TestNet3Params.get(), "forwarding-service-testnet")
      case "regtest" => (RegTestParams.get(), "forwarding-service-regtest")
      case _ => (MainNetParams.get(), "forwarding-service")
    }

    val forwardingAddress = new Address(params, args(0));

    val kit = new BitpagaKit(params, new File("."), filePrefix)

    println("Starting the kit ...")
    kit.startAndWait()
  }
}

class BitpagaKit(p: NetworkParameters, f: File, fp: String) extends WalletAppKit(p,f,fp){

  // This is called in a background thread after startAndWait is called, as setting up various objects
  // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
  // on the main thread.
  override def onSetupCompleted(){
    if (wallet.getKeychainSize < 1){
      println("Wallet has keychainsize = 0")
      val eckey = new ECKey();
      println("Adding eckey = "+eckey)
      wallet().addKey(eckey)
    }

  }
}
