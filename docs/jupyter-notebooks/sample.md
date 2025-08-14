# GWAS REST API

## Introduction
### What is a Jupyter Notebook?

A Jupyter Notebook is an interactive tool for writing and running code, and the code within it is standard Python that you can use anywhere.

The document is organized into individual blocks called cells. 

You can write Python code in a cell, run just that cell. Note that you can see the output of your code beneath your code.  

You can also have another cell in markdown format (as in this cell) where you can explain what you are doing. 

### How to reuse the code

The code in this notebook is portable. You can copy the codes from cells. Feel free to paste them into another Python environment. For example, you can create a Python script (e.g., `my_api_script.py`) or run it in an interactive Python shell in your terminal. 

## Structure of this document

This interactive notebook provides a hands-on guide to using GWAS REST API. 

The first a few cells handle the initial setup by installing all the necessary Python libraries. See [Libraries](#Libraries).

Following that, we define a set of helper functions, which are reusable blocks of code designed to simplify common tasks like sending requests and processing data. See [Helper functions](#Helper-functions).

The final section contains practical examples, showing you how to answer specific questions by calling our API with the provided functions. See [Sample Questions](#Sample-Questions). You can run the cells sequentially to see everything in action and learn how to integrate our API into your own projects.

## Libraries


```python
# Install necessary libraries
!pip install --quiet requests pandas
```

    
    [1m[[0m[34;49mnotice[0m[1;39;49m][0m[39;49m A new release of pip is available: [0m[31;49m25.1.1[0m[39;49m -> [0m[32;49m25.2[0m
    [1m[[0m[34;49mnotice[0m[1;39;49m][0m[39;49m To update, run: [0m[32;49mpip install --upgrade pip[0m



```python
# Import libraries
import requests
import pandas as pd
import json
```


```python
# Set the base URL for the GWAS Catalog REST API
BASE_URL = "https://www.ebi.ac.uk/gwas/rest/api"

# Set pandas display options to show all columns
pd.set_option('display.max_columns', None)

print("Setup complete. Libraries are installed and imported.")
```

    Setup complete. Libraries are installed and imported.


## Helper functions

Now we define a few helper functions that we will use down below.

Please remember to add these helper functions in your code if you are planning to run the codes from this document in your environment.


```python
def gwas_api_request(endpoint, params=None):
    """
    Performs a GET request to the GWAS Catalog REST API.

    Args:
        endpoint (str): The API endpoint to call (e.g., '/v2/studies').
        params (dict, optional): A dictionary of query parameters. Defaults to None.

    Returns:
        dict: The JSON response from the API as a Python dictionary, or None if the request fails.
    """
    full_url = f"{BASE_URL}{endpoint}"
    
    try:
        # Make the GET request
        response = requests.get(full_url, params=params)
        
        # Check if the request was successful (HTTP status code 200)
        if response.status_code == 200:
            return response.json()
        else:
            # Print an error message if the request was not successful
            print(f"Error: Received status code {response.status_code}")
            print(f"URL: {response.url}")
            print(f"Response: {response.text}")
            return None
            
    except requests.exceptions.RequestException as e:
        # Handle network-related errors
        print(f"An error occurred: {e}")
        return None

print("Helper function 'gwas_api_request' is defined.")
```

    Helper function 'gwas_api_request' is defined.



```python
import time

def get_all_variants_for_trait(trait_name):
    """
    Fetches all unique variant rsIDs for a given trait by handling API pagination.

    Args:
        trait_name (str): The name of the trait to query.

    Returns:
        set: A set of unique rsID strings associated with the trait.
    """
    variants = set()
    current_page = 0
    total_pages = 1
    
    print(f"--- Starting search for '{trait_name}' ---")

    # Loop through all pages of the API results
    while current_page < total_pages:
        endpoint = "/v2/associations"
        params = {
            "efo_trait": trait_name, 
            "size": 200, 
            "page": current_page
        }
        
        # Make the API call
        data = gwas_api_request(endpoint, params)
        
        if data and '_embedded' in data:
            associations_list = data['_embedded']['associations']
            
            # On the first request, find out the total number of pages
            if current_page == 0:
                total_pages = data['page']['totalPages']
                # print(f"Found {data['page']['totalElements']} total associations across {total_pages} pages.")

            # Extract the rsID from each association
            for association in associations_list:
                if 'snp_effect_allele' in association and association['snp_effect_allele']:
                    # e.g., from ['rs123-A'], get 'rs123'
                    risk_allele_str = association['snp_effect_allele'][0]
                    rs_id = risk_allele_str.split('-')[0]
                    if rs_id.startswith('rs'):
                        variants.add(rs_id)
            
            # print(f"Page {current_page + 1}/{total_pages} processed. Found {len(variants)} unique variants so far.")
            current_page += 1
            time.sleep(0.1) # Be polite to the API by adding a small delay
        else:
            print("No more data or an error occurred. Stopping.")
            break
            
    print(f"--- Finished fetching for '{trait_name}'. Found {len(variants)} total unique variants. ---\n")
    return variants

print("Helper function 'get_all_variants_for_trait' is defined.")
```

    Helper function 'get_all_variants_for_trait' is defined.



```python
def get_all_associations_for_trait(trait_name):
    """
    Fetches all association objects for a given trait by handling API pagination.

    Args:
        trait_name (str): The name of the trait to query.

    Returns:
        list: A list of all association dictionaries for the trait, sorted by p-value.
    """
    all_associations = []
    current_page = 0
    total_pages = 1  # Initialize to 1 to start the loop
    
    print(f"--- Starting search for all associations related to '{trait_name}' ---")

    # Loop through all pages of the API results
    while current_page < total_pages:
        endpoint = "/v2/associations"
        params = {
            "efo_trait": trait_name, 
            "sort": "p_value",  # Sort by significance from the start
            "direction": "asc",
            "size": 40, 
            "page": current_page
        }
        
        data = gwas_api_request(endpoint, params)
        
        if data and '_embedded' in data:
            associations_list = data['_embedded']['associations']
            all_associations.extend(associations_list)
            
            if current_page == 0:
                total_pages = data['page']['totalPages']
                # print(f"Found {data['page']['totalElements']} total associations across {total_pages} pages.")

            # print(f"Page {current_page + 1}/{total_pages} processed. Collected {len(all_associations)} associations so far.")
            current_page += 1
            time.sleep(0.1) # Be polite to the API
        else:
            print("No more data or an error occurred. Stopping.")
            break
            
    print(f"--- Finished fetching. Found {len(all_associations)} total associations. ---\n")
    return all_associations

print("Helper function 'get_all_associations_for_trait' is defined.")
```

    Helper function 'get_all_associations_for_trait' is defined.



```python
def get_all_genes_for_trait (trait):    
    # Use a set to store unique gene names automatically
    all_genes = set()
    current_page = 0
    total_pages = 1 # Initialize to 1 to start the loop
    
    # Loop through all pages of the API results
    while current_page < total_pages:
        # Define the endpoint and parameters
        endpoint = "/v2/associations"
        params = {
            "efo_trait": disease_of_interest,
            "size": 200,  # Request a larger size per page for efficiency
            "page": current_page
        }
    
        # Make the API call
        associations_data = gwas_api_request(endpoint, params)
    
        # Check if the request was successful and contains data
        if associations_data and '_embedded' in associations_data:
            # Extract the list of associations
            associations_list = associations_data['_embedded']['associations']
            
            # Update the total number of pages from the first response
            if current_page == 0:
                total_pages = associations_data['page']['totalPages']
                # print(f"Found {associations_data['page']['totalElements']} total associations across {total_pages} pages.")
    
            # Process each association in the current page
            for association in associations_list:
                # Check if 'mapped_genes' exists and is not empty
                if 'mapped_genes' in association and association['mapped_genes']:
                    # The field is a list, e.g., ['GENE1', 'GENE2,GENE3']
                    for gene_string in association['mapped_genes']:
                        # A single string can have multiple comma-separated genes
                        genes = gene_string.split(',')
                        for gene in genes:
                            if gene.strip():  # Ensure the gene name isn't empty
                                all_genes.add(gene.strip())
    
            # print(f"Processed page {current_page + 1} of {total_pages}. Found {len(all_genes)} unique genes so far.")
            current_page += 1
        else:
            # Stop if there's no more data or an error occurs
            print("No more data found or an error occurred. Stopping.")
            break

        return all_genes

print("Helper function 'get_all_genes_for_trait' is defined.")
```

    Helper function 'get_all_genes_for_trait' is defined.



```python
def get_all_associations_for_accession_id(accession_id):
    """
    Fetches all association objects for a given trait by handling API pagination.

    Args:
        accession_id (str): The id of the study

    Returns:
        list: A list of all association dictionaries for the study, sorted by p-value.
    """
    all_associations = []
    current_page = 0
    total_pages = 1  # Initialize to 1 to start the loop
    
    print(f"--- Starting search for all associations related to '{accession_id}' ---")

    # Loop through all pages of the API results
    while current_page < total_pages:
        endpoint = "/v2/associations"
        params = {
            "accession_id": accession_id, 
            "sort": "p_value",  # Sort by significance from the start
            "direction": "asc",
            "size": 200, 
            "page": current_page
        }
        
        data = gwas_api_request(endpoint, params)
        
        if data and '_embedded' in data:
            associations_list = data['_embedded']['associations']
            all_associations.extend(associations_list)
            
            if current_page == 0:
                total_pages = data['page']['totalPages']
                print(f"Found {data['page']['totalElements']} total associations across {total_pages} pages.")

            print(f"Page {current_page + 1}/{total_pages} processed. Collected {len(all_associations)} associations so far.")
            current_page += 1
            time.sleep(0.1) # Be polite to the API
        else:
            print("No more data or an error occurred. Stopping.")
            break
            
    print(f"--- Finished fetching. Found {len(all_associations)} total associations. ---\n")
    return all_associations

print("Helper function 'get_all_associations_for_accession_id' is defined.")
```

    Helper function 'get_all_associations_for_accession_id' is defined.


## Sample Questions

Below we have some sample questions and how to answer them using GWAS REST API.

Note that in all questions below, we use the EFO traits for the diseases and tratis. For more information, please visit https://www.ebi.ac.uk/efo/

### Question 1: What variants are associated with a specific disease, e.g. type 2 diabetes?

To find variants associated with "type 2 diabetes mellitus," the code queries the `/v2/associations` API endpoint, using the disease name as a filter. The results are then loaded into a pandas DataFrame for easier data handling. The script proceeds to clean the raw data for clarity, for example by separating the variant's **rsID** from its **risk allele** and formatting the list of associated genes. Finally, it presents a clear table displaying the most important information for the top findings, such as the variant ID, p-value, and the study it was identified in.


```python
# The disease we are interested in
disease_of_interest = "type 2 diabetes mellitus"

# Define the endpoint and parameters for the query
endpoint = "/v2/associations"
params = {
    "efo_trait": disease_of_interest,
    "size": 10  # Let's get the top 10 results for this example
}

# Make the API call using our helper function
associations_data = gwas_api_request(endpoint, params)

# Process and display the response
if associations_data and '_embedded' in associations_data:
    # Extract the list of associations from the '_embedded' key
    associations_list = associations_data['_embedded']['associations']
    
    # Convert the list into a pandas DataFrame
    associations_df = pd.DataFrame(associations_list)
    
    print(f"Found {associations_data['page']['totalElements']} variants associated with '{disease_of_interest}'.")
    print("Displaying the first 10 results:")
    # Parse the 'snp_effect_allele' field. It's a list containing a string like 'rsID-Allele'
    # Extract the string 'rsID-Allele' from the list
    risk_allele_str = associations_df['snp_effect_allele'].str[0]
    
    # Split the string into the variant ID and the allele base
    split_allele = risk_allele_str.str.split('-', n=1, expand=True)
    associations_df['variant_rsID'] = split_allele[0]
    associations_df['risk_allele_base'] = split_allele[1]
    
    # Select, rename, and display the most relevant columns for clarity
    display_df = associations_df[[
        'variant_rsID', 
        'risk_allele_base',
        'p_value', 
        'risk_frequency', 
        'accession_id',
        'mapped_genes'
    ]].copy()

    display_df['mapped_genes'] = display_df['mapped_genes'].apply(
        lambda genes: ', '.join(genes) if isinstance(genes, list) else ''
    )

    display(display_df)
else:
    print(f"No associations found for '{disease_of_interest}' or an error occurred.")

```

    Found 8091 variants associated with 'type 2 diabetes mellitus'.
    Displaying the first 10 results:



<div>
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>variant_rsID</th>
      <th>risk_allele_base</th>
      <th>p_value</th>
      <th>risk_frequency</th>
      <th>accession_id</th>
      <th>mapped_genes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>rs10811661</td>
      <td>?</td>
      <td>3.000000e-08</td>
      <td>NR</td>
      <td>GCST90651603</td>
      <td>CDKN2B-AS1</td>
    </tr>
    <tr>
      <th>1</th>
      <td>rs2237897</td>
      <td>?</td>
      <td>6.000000e-14</td>
      <td>NR</td>
      <td>GCST90651214</td>
      <td>KCNQ1</td>
    </tr>
    <tr>
      <th>2</th>
      <td>rs2237897</td>
      <td>?</td>
      <td>3.000000e-93</td>
      <td>NR</td>
      <td>GCST90651126</td>
      <td>KCNQ1</td>
    </tr>
    <tr>
      <th>3</th>
      <td>rs79511817</td>
      <td>?</td>
      <td>9.000000e-06</td>
      <td>NR</td>
      <td>GCST90651113</td>
      <td>NGEF</td>
    </tr>
    <tr>
      <th>4</th>
      <td>rs929889465</td>
      <td>?</td>
      <td>2.000000e-08</td>
      <td>NR</td>
      <td>GCST90651113</td>
      <td>FTH1P5, TIAL1P1</td>
    </tr>
    <tr>
      <th>5</th>
      <td>rs7933438</td>
      <td>?</td>
      <td>6.000000e-09</td>
      <td>NR</td>
      <td>GCST90651113</td>
      <td>LINC02725</td>
    </tr>
    <tr>
      <th>6</th>
      <td>rs924150</td>
      <td>?</td>
      <td>8.000000e-11</td>
      <td>NR</td>
      <td>GCST90651113</td>
      <td>TSHZ3</td>
    </tr>
    <tr>
      <th>7</th>
      <td>rs9379084</td>
      <td>?</td>
      <td>3.000000e-14</td>
      <td>NR</td>
      <td>GCST90651113</td>
      <td>RREB1</td>
    </tr>
    <tr>
      <th>8</th>
      <td>rs79412043</td>
      <td>?</td>
      <td>2.000000e-06</td>
      <td>NR</td>
      <td>GCST90651113</td>
      <td>LYN</td>
    </tr>
    <tr>
      <th>9</th>
      <td>rs79658946</td>
      <td>?</td>
      <td>7.000000e-08</td>
      <td>NR</td>
      <td>GCST90651113</td>
      <td>NDUFB2</td>
    </tr>
  </tbody>
</table>
</div>


### Question 2: What studies with samples including cohort “UKB” are available for “type 2 diabetes”?

This script finds studies on "type 2 diabetes" specifically within the "UK Biobank" (UKB) cohort by querying the `/v2/studies` API endpoint. It uses both the `disease_trait` and `cohort` as parameters to precisely filter the results. The returned list of studies is then loaded into a pandas DataFrame for easy organization and viewing. Finally, the code displays a clean table of the findings, highlighting key information for each study like its **accession ID**, **PubMed ID**, and **sample size**.


```python
# The trait and cohort we are interested in
disease_of_interest = "type 2 diabetes"
cohort_of_interest = "UKB" # UK Biobank

# Define the endpoint and parameters for the query
endpoint = "/v2/studies"
params = {
    "disease_trait": disease_of_interest,
    "cohort": cohort_of_interest,
    "size": 20 # Get up to 20 results
}

# Make the API call using our helper function
studies_data = gwas_api_request(endpoint, params)

# Process and display the response
if studies_data and '_embedded' in studies_data:
    # Extract the list of studies from the '_embedded' key
    studies_list = studies_data['_embedded']['studies']
    
    # Convert the list of studies into a pandas DataFrame for nice display
    studies_df = pd.DataFrame(studies_list)
    studies_df['cohort'] = studies_df['cohort'].apply(
        lambda cohorts: ', '.join(cohorts) if isinstance(cohorts, list) else ''
    )
    
    print(f"Found {studies_data['page']['totalElements']} studies for '{disease_of_interest}' in the '{cohort_of_interest}' cohort.")
    print("Displaying results:")
    
    # Display the DataFrame with selected, useful columns
    display(studies_df[[
        'accession_id', 
        'pubmed_id', 
        'disease_trait', 
        'initial_sample_size', 
        'cohort'
    ]])
else:
    print(f"No studies found for '{disease_of_interest}' in the '{cohort_of_interest}' cohort or an error occurred.")
```

    Found 9 studies for 'type 2 diabetes' in the 'UKB' cohort.
    Displaying results:



<div>
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>accession_id</th>
      <th>pubmed_id</th>
      <th>disease_trait</th>
      <th>initial_sample_size</th>
      <th>cohort</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>GCST90468151</td>
      <td>39789286</td>
      <td>Type 2 diabetes</td>
      <td>394,626 European ancestry individuals</td>
      <td>UKB</td>
    </tr>
    <tr>
      <th>1</th>
      <td>GCST90444202</td>
      <td>39379762</td>
      <td>Type 2 diabetes</td>
      <td>51,256 African, African American, East Asian, ...</td>
      <td>UKB, MGBB, GERA, AllofUs</td>
    </tr>
    <tr>
      <th>2</th>
      <td>GCST90302887</td>
      <td>37377600</td>
      <td>Type 2 diabetes</td>
      <td>22,670 British ancestry cases, 313,404 British...</td>
      <td>UKB</td>
    </tr>
    <tr>
      <th>3</th>
      <td>GCST90077724</td>
      <td>34662886</td>
      <td>Type 2 diabetes</td>
      <td>3,497 European ancestry cases, 328,257 Europea...</td>
      <td>UKB</td>
    </tr>
    <tr>
      <th>4</th>
      <td>GCST90132186</td>
      <td>35551307</td>
      <td>Type 2 diabetes</td>
      <td>40,737 South Asian ancestry individuals</td>
      <td>Other, ITH, LOLIPOP, PROMIS, RHS, SINDI, UKB</td>
    </tr>
    <tr>
      <th>5</th>
      <td>GCST90132184</td>
      <td>35551307</td>
      <td>Type 2 diabetes</td>
      <td>251,740 European ancestry individuals</td>
      <td>BIOME, DECODE, DGDG, DGI, EGCUT, EPIC, FHS, FU...</td>
    </tr>
    <tr>
      <th>6</th>
      <td>GCST90100587</td>
      <td>34862199</td>
      <td>Type 2 diabetes</td>
      <td>33,139 European ancestry cases, 279,507 Europe...</td>
      <td>UKB, FUSION, WTCCC, GERA, MGBB, other</td>
    </tr>
    <tr>
      <th>7</th>
      <td>GCST90018926</td>
      <td>34594039</td>
      <td>Type 2 diabetes</td>
      <td>38,841 European ancestry cases, 451,248 Europe...</td>
      <td>BBJ, UKB, FinnGen</td>
    </tr>
    <tr>
      <th>8</th>
      <td>GCST90038634</td>
      <td>33959723</td>
      <td>Type 2 diabetes</td>
      <td>3,260 cases, 481,338 controls</td>
      <td>UKB</td>
    </tr>
  </tbody>
</table>
</div>


### Question 3: What are the most significant associations for a specific SNP? eg: significant associations for rs1050316

This script finds the most statistically significant trait associations for a specific SNP, `rs1050316`, by querying the `/v2/associations` endpoint. It instructs the API to sort the results by **p-value** in ascending order, ensuring that the top results are the most significant ones. The returned data is loaded into a pandas DataFrame, and a helper function is used to extract clean trait names from the complex source data. Finally, the code displays a table of the top 10 associations, showing the reported **trait**, its **p-value**, and the study it came from.


```python
# The SNP we are interested in
snp_of_interest = "rs1050316"

# Define the endpoint and parameters for the query
endpoint = "/v2/associations"
params = {
    "rs_id": snp_of_interest,
    "sort": "p_value",  # Sort by p-value
    "direction": "asc", # Sort in ascending order (most significant first)
    "size": 10          # Get the top 10 most significant results
}

# Make the API call
associations_data = gwas_api_request(endpoint, params)

# Process and display the response
if associations_data and '_embedded' in associations_data:
    # Extract the list of associations
    associations_list = associations_data['_embedded']['associations']
    
    # Convert to a pandas DataFrame
    associations_df = pd.DataFrame(associations_list)
    
    print(f"Found {associations_data['page']['totalElements']} associations for SNP '{snp_of_interest}'.")
    print("Displaying the 10 most significant results:")
    
    # The 'efo_traits' column is a list of dictionaries.
    # This helper function extracts the trait name for cleaner display.
    def extract_trait(traits_list):
        if traits_list and isinstance(traits_list, list) and len(traits_list) > 0:
            return traits_list[0].get('efo_trait', 'N/A')
        return 'N/A'

    associations_df['trait_name'] = associations_df['efo_traits'].apply(extract_trait)

    # Display the DataFrame with selected and renamed columns
    display(associations_df[[
        'p_value',
        'trait_name',
        'risk_frequency',
        'accession_id',
        'first_author'
    ]])
else:
    print(f"No associations found for SNP '{snp_of_interest}' or an error occurred.")
```

    Found 12 associations for SNP 'rs1050316'.
    Displaying the 10 most significant results:



<div>
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>p_value</th>
      <th>trait_name</th>
      <th>risk_frequency</th>
      <th>accession_id</th>
      <th>first_author</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>9.000000e-30</td>
      <td>TPE interval measurement</td>
      <td>0.347</td>
      <td>GCST010346</td>
      <td>Ramirez J</td>
    </tr>
    <tr>
      <th>1</th>
      <td>6.000000e-29</td>
      <td>pain</td>
      <td>NR</td>
      <td>GCST90104572</td>
      <td>Mocci E</td>
    </tr>
    <tr>
      <th>2</th>
      <td>4.000000e-22</td>
      <td>pain</td>
      <td>NR</td>
      <td>GCST90104573</td>
      <td>Mocci E</td>
    </tr>
    <tr>
      <th>3</th>
      <td>3.000000e-21</td>
      <td>TPE interval measurement</td>
      <td>0.346</td>
      <td>GCST010346</td>
      <td>Ramirez J</td>
    </tr>
    <tr>
      <th>4</th>
      <td>2.000000e-15</td>
      <td>blood protein amount</td>
      <td>0.3621</td>
      <td>GCST90090292</td>
      <td>Gudjonsson A</td>
    </tr>
    <tr>
      <th>5</th>
      <td>2.000000e-14</td>
      <td>platelet crit</td>
      <td>0.6515</td>
      <td>GCST004607</td>
      <td>Astle WJ</td>
    </tr>
    <tr>
      <th>6</th>
      <td>3.000000e-12</td>
      <td>TPE interval measurement</td>
      <td>0.346</td>
      <td>GCST010346</td>
      <td>Ramirez J</td>
    </tr>
    <tr>
      <th>7</th>
      <td>2.000000e-11</td>
      <td>Headache</td>
      <td>NR</td>
      <td>GCST005337</td>
      <td>Meng W</td>
    </tr>
    <tr>
      <th>8</th>
      <td>3.000000e-11</td>
      <td>body height</td>
      <td>NR</td>
      <td>GCST90435412</td>
      <td>Shi S</td>
    </tr>
    <tr>
      <th>9</th>
      <td>4.000000e-11</td>
      <td>platelet count</td>
      <td>0.6515</td>
      <td>GCST004603</td>
      <td>Astle WJ</td>
    </tr>
  </tbody>
</table>
</div>


### Question 4: Which genes are associated with type 2 diabetes?

This script compiles a comprehensive list of all unique genes associated with "type 2 diabetes mellitus". It uses a custom helper function, `get_all_genes_for_trait`, which is responsible for repeatedly calling the API to fetch all relevant associations and extracting the gene names from them. To ensure the final list is unique, the script gathers all gene names into a **Python set**, which automatically removes any duplicates. Finally, it prints the total count of unique genes found and displays a sorted list of the first 100 genes as an example.


```python
# The disease we want to find associated genes for
disease_of_interest = "type 2 diabetes mellitus"
print(f"Searching for genes associated with '{disease_of_interest}'...")

all_genes = get_all_genes_for_trait(disease_of_interest)

# --- Display the final results ---
print("\n--- Search Complete ---")
if all_genes:
    # Convert the set to a sorted list for clean display
    sorted_genes = sorted(list(all_genes))
    print(f"Found a total of {len(sorted_genes)} unique genes associated with '{disease_of_interest}'.")
    
    # Displaying the first 100 genes as an example
    print("Example genes:", sorted_genes[:100])
else:
    print(f"Could not find any genes associated with '{disease_of_interest}'.")
```

    Searching for genes associated with 'type 2 diabetes mellitus'...
    
    --- Search Complete ---
    Found a total of 248 unique genes associated with 'type 2 diabetes mellitus'.
    Example genes: ['ABO', 'ADA2', 'APOC1', 'APOC1P1', 'ARHGEF38', 'ARID3B', 'ARIH2', 'ARMCX4', 'ATP11B-DT', 'ATP2B3', 'ATP5MGP7', 'ATXN7', 'AUTS2', 'BPTF', 'BRAF', 'C2CD4B', 'C5orf67', 'CAMK1D', 'CCSER1', 'CCT4P1', 'CDC123', 'CDHR18P', 'CDKAL1', 'CDKN2B-AS1', 'CEP120', 'CFAP263', 'CFAP47', 'CHRNB1', 'CIZ1', 'CLBA1', 'COBLL1', 'COX5A', 'CRHR2', 'CT66', 'CTRB2', 'CTXND2', 'CWH43', 'DCUN1D4', 'DHX15', 'DNAH9', 'DNAJB6', 'DOCK3', 'DPPA3P11', 'DPY19L3', 'DUSP9', 'DYRK1A', 'EIF2S2P3', 'ELAPOR1', 'EMB', 'ERCC6L2-AS1', 'ESX1', 'FAF1', 'FAIM2', 'FAM219A', 'FAP', 'FBRSL1', 'FBXL20', 'FGFR3', 'FMNL2', 'FOXP2', 'FTH1P5', 'FTO', 'GALNT8', 'GARS1-DT', 'GCA', 'GDAP1L1', 'GJB1', 'GNAT1', 'GP2', 'GPAT4', 'GPSM1', 'GRB14', 'GRM2', 'GRM8', 'HCN1', 'HHEX', 'HLA-B', 'HNF1B', 'HNF4A', 'IDE', 'IFITM8P', 'IFNL3P1', 'IGF2BP2', 'IL13RA1', 'IL1RAPL2', 'INTS12', 'IQCF6', 'IUR1', 'JAZF1', 'JMJD1C', 'KCNH7', 'KCNQ1', 'KCNQ1OT1', 'KCNU1', 'LEP', 'LINC01148', 'LINC01153', 'LINC01414', 'LINC01901', 'LINC01902']


### Question 5: What are the top significant SNPs for type 2 diabetes in the European population?

This code finds the most significant SNPs for a trait within a specific population, which requires a multi-step process. First, it retrieves a complete list of all associations for "type 2 diabetes mellitus," pre-sorted by p-value. The script then iterates through this sorted list and, for each association, makes a separate API call to fetch the corresponding study's details. It checks if the study's population is "European" and adds the association to a results list if it matches. This process stops once 10 matching associations are found, and the final list is displayed in a clean table.


```python
# --- Configuration ---
trait_of_interest = "type 2 diabetes mellitus"
population_of_interest = "European"
results_to_find = 10
top_associations_in_population = []

pd.options.display.float_format = '{:.10f}'.format

print(f"Searching for top {results_to_find} SNPs for '{trait_of_interest}' in '{population_of_interest}' ancestry studies...")

# --- Step 1: Get all associations for the trait using the new helper function ---
# The list is already sorted by p-value because we specified it in the helper function's API call.
all_trait_associations = get_all_associations_for_trait(trait_of_interest)

# --- Step 2: Loop through the sorted associations and check each study's ancestry ---
if all_trait_associations:
    for association in all_trait_associations:
        accession_id = association.get('accession_id')
        if not accession_id:
            continue

        # Fetch the study details to check its ancestry
        study_data = gwas_api_request(f"/v2/studies/{accession_id}")

        if study_data:
            discovery_ancestry = study_data.get('discovery_ancestry', [])
            if any(population_of_interest.lower() in s.lower() for s in discovery_ancestry):
                top_associations_in_population.append(association)
        
        # Stop once we have found enough results
        if len(top_associations_in_population) >= results_to_find:
            break

# --- Step 3: Display the final results ---
print(f"\n--- Search Complete ---")

# Set pandas display format for floats
pd.options.display.float_format = '{:.20f}'.format

if top_associations_in_population:
    print(f"Found the top {len(top_associations_in_population)} matching associations.")
    
    results_df = pd.DataFrame(top_associations_in_population)
    
    # Parse the risk allele field for cleaner display
    risk_allele_str = results_df['snp_effect_allele'].str[0]
    split_allele = risk_allele_str.str.split('-', n=1, expand=True)
    results_df['variant_rsID'] = split_allele[0]
    results_df['risk_allele_base'] = split_allele[1]

    pd.options.display.float_format = '{:.2e}'.format
    
    # Display the final, cleaned-up DataFrame
    display(results_df[[
        'p_value',
        'variant_rsID',
        'risk_allele_base',
        'accession_id',
        'first_author'
    ]])
else:
    print("Could not find any matching associations.")
```

    Searching for top 10 SNPs for 'type 2 diabetes mellitus' in 'European' ancestry studies...
    --- Starting search for all associations related to 'type 2 diabetes mellitus' ---
    --- Finished fetching. Found 8091 total associations. ---
    
    
    --- Search Complete ---
    Found the top 10 matching associations.



<div>
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>p_value</th>
      <th>variant_rsID</th>
      <th>risk_allele_base</th>
      <th>accession_id</th>
      <th>first_author</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>T</td>
      <td>GCST90492734</td>
      <td>Suzuki K</td>
    </tr>
    <tr>
      <th>1</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>C</td>
      <td>GCST010555</td>
      <td>Vujkovic M</td>
    </tr>
    <tr>
      <th>2</th>
      <td>0.00e+00</td>
      <td>rs35011184</td>
      <td>G</td>
      <td>GCST010557</td>
      <td>Vujkovic M</td>
    </tr>
    <tr>
      <th>3</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>C</td>
      <td>GCST90444202</td>
      <td>Huerta-Chagoya A</td>
    </tr>
    <tr>
      <th>4</th>
      <td>0.00e+00</td>
      <td>rs2237897</td>
      <td>C</td>
      <td>GCST90492734</td>
      <td>Suzuki K</td>
    </tr>
    <tr>
      <th>5</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>T</td>
      <td>GCST90132183</td>
      <td>Mahajan A</td>
    </tr>
    <tr>
      <th>6</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>T</td>
      <td>GCST009379</td>
      <td>Mahajan A</td>
    </tr>
    <tr>
      <th>7</th>
      <td>0.00e+00</td>
      <td>rs7766070</td>
      <td>A</td>
      <td>GCST90492734</td>
      <td>Suzuki K</td>
    </tr>
    <tr>
      <th>8</th>
      <td>0.00e+00</td>
      <td>rs10811661</td>
      <td>T</td>
      <td>GCST90492734</td>
      <td>Suzuki K</td>
    </tr>
    <tr>
      <th>9</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>T</td>
      <td>GCST006867</td>
      <td>Xue A</td>
    </tr>
  </tbody>
</table>
</div>


### Question 6: Which variants are common between "type 2 diabetes" and "obesity"?

This script identifies genetic variants common to both "type 2 diabetes" and "obesity" by comparing two distinct sets of data. It first uses a helper function to retrieve all variants associated with "type 2 diabetes" and stores them in a **set**. The process is then repeated for "obesity", creating a second set of associated variants. Finally, the code uses the `.intersection()` method—a highly efficient operation for sets—to find and display a list of variants present in both groups.


```python
import time

# --- Main script ---
# Define the two traits of interest
trait_1 = "type 2 diabetes mellitus"
trait_2 = "obesity"

# Get the set of variants for each trait
variants_for_trait_1 = get_all_variants_for_trait(trait_1)
variants_for_trait_2 = get_all_variants_for_trait(trait_2)

# Find the intersection of the two sets
common_variants = variants_for_trait_1.intersection(variants_for_trait_2)

# --- Display the final results ---
print("\n--- Search Complete ---")
if common_variants:
    print(f"Found {len(common_variants)} variants common to both '{trait_1}' and '{trait_2}':")
    # Display the list of common variants
    display(sorted(list(common_variants)))
else:
    print(f"No common variants found between '{trait_1}' and '{trait_2}'.")
```

    --- Starting search for 'type 2 diabetes mellitus' ---
    --- Finished fetching for 'type 2 diabetes mellitus'. Found 4785 total unique variants. ---
    
    --- Starting search for 'obesity' ---
    --- Finished fetching for 'obesity'. Found 438 total unique variants. ---
    
    
    --- Search Complete ---
    Found 39 variants common to both 'type 2 diabetes mellitus' and 'obesity':



    ['rs10938397',
     'rs11263770',
     'rs11642015',
     'rs1229984',
     'rs1296328',
     'rs13028310',
     'rs13096210',
     'rs13107325',
     'rs13130484',
     'rs1412265',
     'rs1421085',
     'rs1556036',
     'rs1558902',
     'rs16862964',
     'rs1765131',
     'rs17770336',
     'rs1800437',
     'rs1955851',
     'rs2239647',
     'rs2306593',
     'rs2307111',
     'rs261966',
     'rs2943650',
     'rs34811474',
     'rs34872471',
     'rs429358',
     'rs476828',
     'rs4776970',
     'rs4923464',
     'rs539515',
     'rs56094641',
     'rs633715',
     'rs6567160',
     'rs6595551',
     'rs7132908',
     'rs7185735',
     'rs7498798',
     'rs7903146',
     'rs8050136']


### Question 7: What are the most commonly mapped genes for a specific disease? e.g. type 2 diabetes

This script identifies the most frequently mapped genes for "type 2 diabetes mellitus" by first fetching all available associations for that disease. It then processes this complete list of associations, extracting every gene name from the `mapped_genes` field and handling cases where multiple genes are listed together. All the individual gene names are gathered into a single large list. Finally, the code uses pandas to efficiently count the occurrences of each unique gene and displays a ranked list of the top 10 most common ones.


```python
from collections import Counter

trait_of_interest = "type 2 diabetes mellitus"
results_to_find = 10

print(f"Searching for the most reported genes in associations for '{trait_of_interest}'...")

# --- Step 1: Get all associations for the trait ---
# This reuses the helper function from the previous question.
all_trait_associations = get_all_associations_for_trait(trait_of_interest)

# --- Step 2: Extract and count all gene mentions ---
all_gene_mentions = []
if all_trait_associations:
    for association in all_trait_associations:
        # Check if 'mapped_genes' exists and is not empty
        if 'mapped_genes' in association and association['mapped_genes']:
            # The field is a list, e.g., ['GENE1', 'GENE2,GENE3']
            for gene_string in association['mapped_genes']:
                # A single string can have multiple comma-separated genes
                genes = [gene.strip() for gene in gene_string.split(',') if gene.strip()]
                all_gene_mentions.extend(genes)

# --- Step 3: Display the top N most common genes ---
print(f"\n--- Analysis Complete ---")
if all_gene_mentions:
    # Use pandas Series and value_counts() for easy counting and display
    gene_counts = pd.Series(all_gene_mentions).value_counts()
    
    print(f"Found {len(gene_counts)} unique genes in total.")
    print(f"The top {results_to_find} most frequently reported genes for '{trait_of_interest}' are:")
    
    # Display the top genes
    display(gene_counts.head(results_to_find))
else:
    print(f"Could not find any gene associations for '{trait_of_interest}'.")

```

    Searching for the most reported genes in associations for 'type 2 diabetes mellitus'...
    --- Starting search for all associations related to 'type 2 diabetes mellitus' ---
    --- Finished fetching. Found 8091 total associations. ---
    
    
    --- Analysis Complete ---
    Found 1999 unique genes in total.
    The top 10 most frequently reported genes for 'type 2 diabetes mellitus' are:



    TCF7L2        168
    KCNQ1         129
    CDKN2B-AS1    104
    Y_RNA          97
    CDKAL1         91
    IGF2BP2        64
    SLC30A8        62
    MTNR1B         59
    HNF4A          54
    PPARG          51
    Name: count, dtype: int64


### Question 8: What traits are associated with a particular SNP?

This script comprehensively finds all unique traits associated with an SNP (`rs1050316`) by repeatedly querying the `/v2/associations` endpoint. Because the API returns a large number of results across multiple pages, the code loops through each page one by one to gather all the data. As it processes each association, it extracts the trait names and adds them to a `set` data structure, which automatically prevents duplicates. After retrieving and processing all pages, the script displays the total count and a final, sorted list of every unique trait linked to that SNP.


```python
import pandas as pd
import time


# Configuration
snp_of_interest = "rs1050316"
associated_traits = set() # Use a set to automatically store unique trait names

print(f"Searching for all traits associated with SNP '{snp_of_interest}'...")

# --- Step 1: Paginate through all associations for the SNP ---
current_page = 0
total_pages = 1 # Initialize to 1 to start the loop

while current_page < total_pages:
    endpoint = "/v2/associations"
    params = {
        "rs_id": snp_of_interest,
        "size": 200,
        "page": current_page
    }

    data = gwas_api_request(endpoint, params)
    
    if data and '_embedded' in data:
        associations_list = data['_embedded']['associations']
        
        # On the first request, find out the total number of pages
        if current_page == 0:
            total_pages = data['page']['totalPages']
            print(f"Found {data['page']['totalElements']} total associations across {total_pages} pages.")

        # --- Step 2: Extract the trait name from each association ---
        for association in associations_list:
            # The 'efo_traits' field is a list of dictionaries
            if 'efo_traits' in association and association['efo_traits']:
                for trait_info in association['efo_traits']:
                    if 'efo_trait' in trait_info:
                        associated_traits.add(trait_info['efo_trait'])
        
        print(f"Page {current_page + 1}/{total_pages} processed. Found {len(associated_traits)} unique traits so far.")
        current_page += 1
        time.sleep(0.1) # Be polite to the API
    else:
        # Stop if there's no more data or an error occurs
        print("No more data found or an error occurred. Stopping.")
        break

# --- Step 3: Display the final results ---
print(f"\n--- Search Complete ---")
if associated_traits:
    # Convert the set to a sorted list for clean display
    sorted_traits = sorted(list(associated_traits))
    
    print(f"Found a total of {len(sorted_traits)} unique traits associated with '{snp_of_interest}':")
    
    # Display the list of traits
    for trait in sorted_traits:
        print(f"- {trait}")
else:
    print(f"Could not find any traits associated with '{snp_of_interest}'.")
```

    Searching for all traits associated with SNP 'rs1050316'...
    Found 12 total associations across 1 pages.
    Page 1/1 processed. Found 8 unique traits so far.
    
    --- Search Complete ---
    Found a total of 8 unique traits associated with 'rs1050316':
    - Headache
    - TPE interval measurement
    - blood protein amount
    - body height
    - pain
    - platelet count
    - platelet crit
    - serum alanine aminotransferase amount


### Question 9: Which studies on a trait have full summary statistics?

This script finds all studies for "type 2 diabetes" that have complete summary statistics by querying the `/v2/studies` endpoint. It uses the specific parameter `full_pvalue_set=True` to filter for only those studies with available data. Since the results may span multiple pages, the code uses a `while` loop to automatically fetch every page from the API until all matching studies are collected. Once the loop is complete, all the gathered results are combined into a single pandas DataFrame and displayed in a table, showing each study's **accession ID** and the **link** to its summary statistics file.


```python
trait_of_interest = "type 2 diabetes mellitus"
studies_with_sum_stats = [] # A list to store the resulting study objects

print(f"Searching for studies on '{trait_of_interest}' with full summary statistics...")

# --- Step 1: Paginate through all matching studies ---
current_page = 0
total_pages = 1 # Initialize to 1 to start the loop

while current_page < total_pages:
    endpoint = "/v2/studies"
    params = {
        "efo_trait": trait_of_interest,
        "full_pvalue_set": True, # Filter for studies with summary stats
        "size": 200,
        "page": current_page
    }

    data = gwas_api_request(endpoint, params)
    
    if data and '_embedded' in data:
        studies_list = data['_embedded']['studies']
        studies_with_sum_stats.extend(studies_list)
        
        # On the first request, find out the total number of pages
        if current_page == 0:
            total_pages = data['page']['totalPages']
            print(f"Found {data['page']['totalElements']} total matching studies across {total_pages} pages.")

        print(f"Page {current_page + 1}/{total_pages} processed. Collected {len(studies_with_sum_stats)} studies so far.")
        current_page += 1
        time.sleep(0.1) # Be polite to the API
    else:
        print("No more data found or an error occurred. Stopping.")
        break

# --- Step 2: Display the final results ---
print(f"\n--- Search Complete ---")
if studies_with_sum_stats:
    # Convert the list of studies to a DataFrame
    results_df = pd.DataFrame(studies_with_sum_stats)
    
    print(f"Found a total of {len(results_df)} studies for '{trait_of_interest}' with full summary statistics.")

    # Display relevant columns from the DataFrame
    display(results_df[[
        'accession_id',
        'pubmed_id',
        'initial_sample_size',
        'full_summary_stats' # This column contains the link to the stats
    ]])
else:
    print(f"Could not find any studies for '{trait_of_interest}' with full summary statistics.")
```

    Searching for studies on 'type 2 diabetes mellitus' with full summary statistics...
    Found 97 total matching studies across 1 pages.
    Page 1/1 processed. Collected 97 studies so far.
    
    --- Search Complete ---
    Found a total of 97 studies for 'type 2 diabetes mellitus' with full summary statistics.



<div>
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>accession_id</th>
      <th>pubmed_id</th>
      <th>initial_sample_size</th>
      <th>full_summary_stats</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>GCST90468151</td>
      <td>39789286</td>
      <td>394,626 European ancestry individuals</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>1</th>
      <td>GCST90528075</td>
      <td>40210677</td>
      <td>12,634 Hispanic or Latino cases, 20,102 Hispan...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>2</th>
      <td>GCST90528074</td>
      <td>40210677</td>
      <td>23,541 Hispanic or Latino cases, 37,434 Hispan...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>3</th>
      <td>GCST90479885</td>
      <td>39024449</td>
      <td>51,551 African American or Afro-Caribbean case...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>4</th>
      <td>GCST90477314</td>
      <td>39024449</td>
      <td>2,272 East Asian ancestry cases, 4,333 East As...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>...</th>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
    </tr>
    <tr>
      <th>92</th>
      <td>GCST005898</td>
      <td>29358691</td>
      <td>5,277 European ancestry cases, 15,702 European...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>93</th>
      <td>GCST005413</td>
      <td>29358691</td>
      <td>up to 12,931 European ancestry cases, up to 57...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>94</th>
      <td>GCST005047</td>
      <td>22885922</td>
      <td>6,377 European ancestry male cases, 5,794 Euro...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>95</th>
      <td>GCST004773</td>
      <td>28566273</td>
      <td>up to 26,676 European ancestry cases, up to 13...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>96</th>
      <td>GCST000028</td>
      <td>17463246</td>
      <td>1,464 European ancestry cases, 1,467 European ...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
  </tbody>
</table>
<p>97 rows × 4 columns</p>
</div>


### Question 10: What variants are in a gene `HBS1L` and what are their associated traits?

This is a multi-step script that first finds all variants within a specified gene and then identifies the traits associated with those variants. To start, it queries the `/v2/single-nucleotide-polymorphisms` endpoint using the gene name, `HBS1L`, looping through multiple pages of results to gather a complete list of all variants in that gene. Next, the script iterates through that list of variants, making a new API call for each one to the `/v2/associations` endpoint to find any linked diseases or traits. The collected data is then structured into a pandas DataFrame where each variant is mapped to a list of its associated traits. Finally, to create a clear output, this DataFrame is "exploded" so that each row shows a single variant-to-trait relationship.


```python
# --- Configuration ---
gene_of_interest = "HBS1L"

print(f"--- Step 1: Finding variants for gene '{gene_of_interest}' ---")

# --- Find all variants for the gene, handling pagination ---
gene_variants_rsids = []
current_page = 0
total_pages = 1
while current_page < total_pages:
    endpoint = "/v2/single-nucleotide-polymorphisms"
    params = {"mapped_gene": gene_of_interest, "size": 10000, "page": current_page}
    data = gwas_api_request(endpoint, params)
    
    if data and '_embedded' in data:
        snp_list = data['_embedded']['snps']
        for snp in snp_list:
            if 'rs_id' in snp:
                gene_variants_rsids.append(snp['rs_id'])
        
        if current_page == 0:
            total_pages = data['page']['totalPages']
        # print(f"Page {current_page + 1}/{total_pages} processed. Found {len(gene_variants_rsids)} variants so far.")
        current_page += 1
        time.sleep(0.1) # Be polite
    else:
        break

print(f"\nFound {len(gene_variants_rsids)} total variants for '{gene_of_interest}'.")

# --- Step 2: For the first few variants, find their associated traits ---
variant_trait_map = {}
variants_to_query = gene_variants_rsids[:len(gene_variants_rsids)]

print(f"\n--- Step 2: Checking for associated traits for the first {len(variants_to_query)} variants ---")

for rs_id in variants_to_query:
    traits_for_snp = set()
    endpoint = "/v2/associations"
    params = {"rs_id": rs_id, "size": 200}
    assoc_data = gwas_api_request(endpoint, params)
    
    if assoc_data and '_embedded' in assoc_data:
        for association in assoc_data['_embedded']['associations']:
            if 'efo_traits' in association and association['efo_traits']:
                for trait_info in association['efo_traits']:
                    if 'efo_trait' in trait_info:
                        traits_for_snp.add(trait_info['efo_trait'])
    
    variant_trait_map[rs_id] = sorted(list(traits_for_snp))
    # print(f"Found {len(traits_for_snp)} traits for {rs_id}.")
    time.sleep(0.1) # Be polite

# --- Step 3: Display the results ---
print("\n--- Results ---")

# Convert the map to a list of dictionaries for DataFrame creation
display_data = [{'variant_rsID': key, 'associated_traits': value} for key, value in variant_trait_map.items()]
results_df = pd.DataFrame(display_data)

# Explode the list of traits into separate rows for better readability
results_df_exploded = results_df.explode('associated_traits').reset_index(drop=True)

if not results_df_exploded.empty:
    display(results_df_exploded)
else:
    print("Could not find any trait associations for the variants checked.")
```

    --- Step 1: Finding variants for gene 'HBS1L' ---
    
    Found 178 total variants for 'HBS1L'.
    
    --- Step 2: Checking for associated traits for the first 178 variants ---
    
    --- Results ---



<div>
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>variant_rsID</th>
      <th>associated_traits</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>rs35775236</td>
      <td>hematological measurement</td>
    </tr>
    <tr>
      <th>1</th>
      <td>rs200202852</td>
      <td>hematological measurement</td>
    </tr>
    <tr>
      <th>2</th>
      <td>rs9402693</td>
      <td>hematological measurement</td>
    </tr>
    <tr>
      <th>3</th>
      <td>rs9399139</td>
      <td>hematological measurement</td>
    </tr>
    <tr>
      <th>4</th>
      <td>rs6913541</td>
      <td>hematological measurement</td>
    </tr>
    <tr>
      <th>...</th>
      <td>...</td>
      <td>...</td>
    </tr>
    <tr>
      <th>591</th>
      <td>rs9399137</td>
      <td>monocyte count</td>
    </tr>
    <tr>
      <th>592</th>
      <td>rs9399137</td>
      <td>platelet count</td>
    </tr>
    <tr>
      <th>593</th>
      <td>rs9399137</td>
      <td>red cell distribution width</td>
    </tr>
    <tr>
      <th>594</th>
      <td>rs9399137</td>
      <td>total cholesterol measurement</td>
    </tr>
    <tr>
      <th>595</th>
      <td>rs9399137</td>
      <td>transferrin receptor protein 1 measurement</td>
    </tr>
  </tbody>
</table>
<p>596 rows × 2 columns</p>
</div>


### Question 11: Which variants are associated with an environmental factor on disease outcome? Eg: variants associated with diet on total blood protein measurement (GxE)

This script performs a complex two-step search to find Gene-by-Environment (GxE) interactions, where a factor like **"diet"** influences a trait like **"total blood protein measurement."** First, it queries the `/v2/studies` endpoint to gather all studies that are specifically flagged as GxE (`gxe: True`) for the main trait of interest, handling pagination to ensure a complete list. In the second step, the code iterates through each of those studies, fetching all their internal associations to find the ones that explicitly mention the **"diet"** environmental factor. Finally, these filtered results are organized into a pandas DataFrame and displayed in a table, showing the specific studies where these GxE interactions were reported.


```python
# Function to extract 'efo_trait' and join into a string
def extract_and_join_efo_traits(efo_list):
    if isinstance(efo_list, list):
        return ', '.join([d.get('efo_trait', '') for d in efo_list])
    return ''
```


```python
# --- Configuration ---
trait_of_interest = "total blood protein measurement"
environmental_factor = "diet"
gxe_associations = []

print(f"===== Step 1: Finding GxE studies for '{trait_of_interest}' =====")

# --- Find all GxE studies for the trait, handling pagination ---
gxe_study_accessions = []
current_page = 0
total_pages = 1
while current_page < total_pages:
    endpoint = "/v2/studies"
    params = {
        "efo_trait": trait_of_interest,
        "gxe": True, # Filter for GxE studies
        "size": 200, 
        "page": current_page
    }
    data = gwas_api_request(endpoint, params)
    
    if data and '_embedded' in data:
        studies_list = data['_embedded']['studies']
        for study in studies_list:
            if 'accession_id' in study:
                gxe_study_accessions.append(study['accession_id'])
        
        if current_page == 0:
            total_pages = data['page']['totalPages']
        # print(f"Page {current_page + 1}/{total_pages} processed. Found {len(gxe_study_accessions)} GxE studies.")
        current_page += 1
        time.sleep(0.1)
    else:
        break

print(f"\nFound {len(gxe_study_accessions)} GxE studies for '{trait_of_interest}'.")

# --- Step 2: Find associations with the environmental factor within those studies ---
print(f"\n===== Step 2: Searching for '{environmental_factor}' factor within these studies =====")
if gxe_study_accessions:
    for accession_id in gxe_study_accessions:
        # Get all associations for the current study
        study_associations = get_all_associations_for_accession_id(accession_id)
        
        for association in study_associations:
            efo_traits = association.get('efo_traits')
            if any([environmental_factor.lower() in t.get('efo_trait').lower() for t in efo_traits]):
                gxe_associations.append(association)
        # print(f"Checked study {accession_id}, found {len(gxe_associations)} matching associations so far.")

# --- Step 3: Display the results ---
print(f"\n===== Step 3: Results =====")
if gxe_associations:
    print(f"Found {len(gxe_associations)} associations matching the GxE criteria.")
 
    results_df = pd.DataFrame(gxe_associations)
    
    # Apply the transformations
    results_df['reported_traits_formatted'] = results_df['reported_trait'].apply(lambda x: ', '.join(x) if isinstance(x, list) else '')
    results_df['efo_traits_formatted'] = results_df['efo_traits'].apply(extract_and_join_efo_traits)
    
    pd.options.display.float_format = '{:.20f}'.format
    
    display(results_df[[
        'accession_id',
        'pubmed_id',
        'reported_traits_formatted',
        'efo_traits_formatted'
    ]])
    
    pd.reset_option('display.float_format')
else:
    print("Could not find any GxE associations matching the specified criteria.")
```

    ===== Step 1: Finding GxE studies for 'total blood protein measurement' =====
    
    Found 3 GxE studies for 'total blood protein measurement'.
    
    ===== Step 2: Searching for 'diet' factor within these studies =====
    --- Starting search for all associations related to 'GCST90161226' ---
    Found 10 total associations across 1 pages.
    Page 1/1 processed. Collected 10 associations so far.
    --- Finished fetching. Found 10 total associations. ---
    
    --- Starting search for all associations related to 'GCST90161196' ---
    Found 10 total associations across 1 pages.
    Page 1/1 processed. Collected 10 associations so far.
    --- Finished fetching. Found 10 total associations. ---
    
    --- Starting search for all associations related to 'GCST90026658' ---
    Found 266 total associations across 2 pages.
    Page 1/2 processed. Collected 200 associations so far.
    Page 2/2 processed. Collected 266 associations so far.
    --- Finished fetching. Found 266 total associations. ---
    
    
    ===== Step 3: Results =====
    Found 20 associations matching the GxE criteria.



<div>
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>accession_id</th>
      <th>pubmed_id</th>
      <th>reported_traits_formatted</th>
      <th>efo_traits_formatted</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>1</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>2</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>3</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>4</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>5</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>6</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>7</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>8</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>9</th>
      <td>GCST90161226</td>
      <td>38990837</td>
      <td>Total protein levels (adjusted for BMI) x vege...</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>10</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>11</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>12</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>13</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>14</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>15</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>16</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>17</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>18</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
    <tr>
      <th>19</th>
      <td>GCST90161196</td>
      <td>38990837</td>
      <td>Total protein levels x vegetarianism interaction</td>
      <td>diet measurement, total blood protein measurement</td>
    </tr>
  </tbody>
</table>
</div>


### Question 12: Which genomic region has the most variants associated with a disease? eg: the genomic region with most variants for type 2 diabetes

This script determines which genomic regions are most associated with a disease through a multi-step process. First, it gathers all known genetic associations for "type 2 diabetes mellitus" to compile a comprehensive list of unique variants (rsIDs) linked to the trait. Next, it iterates through every one of these unique variants, making an individual API call for each to the `/v2/single-nucleotide-polymorphisms/` endpoint to find its specific genomic location. After collecting all the region names, the script counts how many times each region appeared. Finally, it displays a ranked list of the most common regions, highlighting which areas of the genome contain the highest number of variants associated with the disease.


```python
import pandas as pd
import time
from collections import Counter

# --- Configuration ---
trait_of_interest = "type 2 diabetes mellitus"
associations_to_check = 100
top_regions_to_display = 10

print(f"--- Step 1: Finding the top {associations_to_check} most significant associations for '{trait_of_interest}' ---")

data = get_all_associations_for_trait(trait_of_interest)
unique_rsids = set()

if data:
    for association in data:
        if 'snp_effect_allele' in association and association['snp_effect_allele']:
            rs_id = association['snp_effect_allele'][0].split('-')[0]
            if rs_id.startswith('rs'):
                unique_rsids.add(rs_id)

print(f"Found {len(unique_rsids)} unique variants from the top associations.")

# --- Step 2: For each unique variant, find its genomic region ---
region_list = []
if unique_rsids:
    print(f"\n--- Step 2: Looking up genomic regions for {len(unique_rsids)} variants ---")
    
    for i, rs_id in enumerate(list(unique_rsids)):
        snp_data = gwas_api_request(f"/v2/single-nucleotide-polymorphisms/{rs_id}")
        
        if snp_data and 'locations' in snp_data:
            for location in snp_data['locations']:
                if 'region' in location and 'name' in location['region']:
                    region_list.append(location['region']['name'])
        
        # Print progress update
        if (i + 1) % 500 == 0:
            print(f"Processed {i + 1}/{len(unique_rsids)} variants...")
        time.sleep(0.1) # Be polite to the API

# --- Step 3: Count and display the most common regions ---
print(f"\n--- Analysis Complete ---")
if region_list:
    # Use pandas Series and value_counts() for easy counting and display
    region_counts = pd.Series(region_list).value_counts()
    
    print(f"The top {top_regions_to_display} genomic regions with the most variants for '{trait_of_interest}' are:")
    display(region_counts.head(top_regions_to_display))
else:
    print("Could not retrieve region information for the variants.")
```

    --- Step 1: Finding the top 100 most significant associations for 'type 2 diabetes mellitus' ---
    --- Starting search for all associations related to 'type 2 diabetes mellitus' ---
    --- Finished fetching. Found 8091 total associations. ---
    
    Found 2884 unique variants from the top associations.
    
    --- Step 2: Looking up genomic regions for 2884 variants ---
    Processed 500/2884 variants...
    Processed 1000/2884 variants...
    Processed 1500/2884 variants...
    Processed 2000/2884 variants...
    Processed 2500/2884 variants...
    
    --- Analysis Complete ---
    The top 10 genomic regions with the most variants for 'type 2 diabetes mellitus' are:



    10q25.2     51
    12q24.31    47
    9p21.3      45
    11p15.5     43
    11p15.4     34
    6p22.3      34
    5q11.2      30
    3q27.2      29
    10q23.33    28
    20q13.12    26
    Name: count, dtype: int64


### Question 13. Which SNP has the strongest effect size/OR for a disease? eg: The SNP with the strongest effect size/OR for type 2 diabetes

This script identifies the single SNP with the strongest effect size, measured by **Odds Ratio** (OR), for "type 2 diabetes mellitus". It queries the `/v2/associations` endpoint, instructing the API to sort associations by `or_value` in descending order and to only return the single top result. The code then places this single top association into a pandas DataFrame for a clean presentation. Finally, it displays a table showing the SNP with the highest OR, its **p-value**, and its **rsID**.


```python
# --- Configuration ---
trait_of_interest = "type 2 diabetes mellitus"

print(f"Searching for the SNP with the strongest Odds Ratio for '{trait_of_interest}'...")

# --- Step 1: Query the API and sort by or_value ---
endpoint = "/v2/associations"
params = {
    "efo_trait": trait_of_interest,
    "sort": "or_value",      # Sort by Odds Ratio
    "direction": "desc",    # Sort descending to get the highest value first
    "size": 1               # We only need the top result
}

data = gwas_api_request(endpoint, params)

# --- Step 2: Display the result ---
if data and '_embedded' in data and data['_embedded']['associations']:
    # Extract the single top association
    top_association = data['_embedded']['associations'][0]
    
    print("\nFound the following association with the strongest effect size (Odds Ratio):")
    
    # For nice display, convert the single dictionary to a DataFrame
    results_df = pd.DataFrame([top_association])
    
    # Parse the risk allele field
    risk_allele_str = results_df['snp_effect_allele'].str[0]
    split_allele = risk_allele_str.str.split('-', n=1, expand=True)
    results_df['variant_rsID'] = split_allele[0]
    results_df['risk_allele_base'] = split_allele[1]

    # Set display format for floats
    pd.options.display.float_format = '{:.2e}'.format

    display(results_df[[
        'or_value',
        'p_value',
        'variant_rsID',
        'risk_frequency',
        'accession_id'
    ]])
    
    pd.reset_option('display.float_format')
else:
    print(f"\nCould not find an association with an Odds Ratio for '{trait_of_interest}'.")

```

    Searching for the SNP with the strongest Odds Ratio for 'type 2 diabetes mellitus'...
    
    Found the following association with the strongest effect size (Odds Ratio):



<div>
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>or_value</th>
      <th>p_value</th>
      <th>variant_rsID</th>
      <th>risk_frequency</th>
      <th>accession_id</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>17.692</td>
      <td>9.00e-07</td>
      <td>rs77989445</td>
      <td>0.1436</td>
      <td>GCST006484</td>
    </tr>
  </tbody>
</table>
</div>


### Question 14. How many studies report a specific gene associated with a specific disease? eg: number of the studies reporting the KCNQ1 gene associated with type 2 diabetes

This script finds how many unique studies associate the **KCNQ1** gene with "type 2 diabetes." First, it retrieves all known associations for the disease using a helper function. The script then iterates through these results locally, checking the `mapped_genes` field of each association for a mention of **KCNQ1**. To ensure each study is counted only once, it uses a `set` to collect the unique study `accession_id`s from any matching records. Finally, it reports the total number of unique studies found and prints a list of their accession IDs.


```python
# --- Configuration ---
trait_of_interest = "type 2 diabetes mellitus"
gene_of_interest = "KCNQ1"
reporting_studies = set() # Use a set to store unique study accession_ids

print(f"Searching for the number of studies reporting '{gene_of_interest}' associated with '{trait_of_interest}'...")

# --- Step 1: Get all associations for the trait ---
# This reuses the helper function from previous questions.
# Make sure `get_all_associations_for_trait` is defined in your notebook.
all_trait_associations = get_all_associations_for_trait(trait_of_interest)

# --- Step 2: Filter associations by gene and collect unique study IDs ---
if all_trait_associations:
    for association in all_trait_associations:
        # Check if 'mapped_genes' exists and is not empty
        if 'mapped_genes' in association and association['mapped_genes']:
            # A single string can have multiple comma-separated genes, so we must check carefully
            found_gene = False
            for gene_string in association['mapped_genes']:
                if gene_of_interest in gene_string.split(','):
                    found_gene = True
                    break
            
            # If the gene was found, add the study's accession ID to our set
            if found_gene and 'accession_id' in association:
                reporting_studies.add(association['accession_id'])

# --- Step 3: Display the result ---
print(f"\n--- Search Complete ---")
if reporting_studies:
    final_count = len(reporting_studies)
    print(f"A total of {final_count} unique studies report the '{gene_of_interest}' gene in an association with '{trait_of_interest}'.")
    
    print("\nStudy Accession IDs:")
    # Display the list of unique study IDs
    for accession_id in sorted(list(reporting_studies)):
        print(f"- {accession_id}")
else:
    print(f"Could not find any studies reporting '{gene_of_interest}' for '{trait_of_interest}'.")
```

    Searching for the number of studies reporting 'KCNQ1' associated with 'type 2 diabetes mellitus'...
    --- Starting search for all associations related to 'type 2 diabetes mellitus' ---
    --- Finished fetching. Found 8091 total associations. ---
    
    
    --- Search Complete ---
    A total of 43 unique studies report the 'KCNQ1' gene in an association with 'type 2 diabetes mellitus'.
    
    Study Accession IDs:
    - GCST000219
    - GCST000221
    - GCST000383
    - GCST000601
    - GCST001173
    - GCST001666
    - GCST002128
    - GCST002317
    - GCST002560
    - GCST003400
    - GCST004758
    - GCST004894
    - GCST005047
    - GCST006801
    - GCST006867
    - GCST007847
    - GCST008833
    - GCST009379
    - GCST010118
    - GCST010436
    - GCST010553
    - GCST010554
    - GCST010555
    - GCST010557
    - GCST012043
    - GCST90013693
    - GCST90018706
    - GCST90018926
    - GCST90132183
    - GCST90132184
    - GCST90132185
    - GCST90132186
    - GCST90132187
    - GCST90137502
    - GCST90161239
    - GCST90255648
    - GCST90444202
    - GCST90475666
    - GCST90479885
    - GCST90492734
    - GCST90528074
    - GCST90651113
    - GCST90651126


### Question 15. What are the sample sizes used for the top 10 significant variants for disease, eg: find the top 10 significant variants associated with type 2 diabetes and find their sample size

This script answers the question in a two-step process. First, it makes an API call to the `/v2/associations` endpoint to find the top 10 most significant variants for "type 2 diabetes mellitus," sorted by **p-value**. Then, the script iterates through each of these results and makes a second, separate API call to the `/v2/studies` endpoint using the study's unique **accession ID**. This second call fetches the details for that specific study, including its initial sample size. Finally, the sample size is added to the variant information, and a combined table is displayed, linking each top variant to the sample size of the study it was found in.


```python
# --- Configuration ---
trait_of_interest = "type 2 diabetes mellitus"
results_to_find = 10
final_results = []

print(f"--- Step 1: Finding the top {results_to_find} most significant associations for '{trait_of_interest}' ---")

# --- Get top 10 associations for the trait ---
endpoint = "/v2/associations"
params = {
    "efo_trait": trait_of_interest,
    "sort": "p_value",
    "direction": "asc",
    "size": results_to_find
}
data = gwas_api_request(endpoint, params)

if data and '_embedded' in data:
    top_associations = data['_embedded']['associations']
    print(f"Found {len(top_associations)} associations. Now fetching their study sample sizes...")

    # --- Step 2: For each association, find its study's sample size ---
    for association in top_associations:
        accession_id = association.get('accession_id')
        if not accession_id:
            continue

        # Make a second API call to get study details
        study_data = gwas_api_request(f"/v2/studies/{accession_id}")
        
        # Add the sample size to our association object
        if study_data and 'initial_sample_size' in study_data:
            association['initial_sample_size'] = study_data['initial_sample_size']
        else:
            association['initial_sample_size'] = "N/A"
        
        final_results.append(association)
        time.sleep(0.1) # Be polite to the API

# --- Step 3: Display the results ---
print(f"\n--- Search Complete ---")
if final_results:
    results_df = pd.DataFrame(final_results)
    
    # Parse the risk allele field
    risk_allele_str = results_df['snp_effect_allele'].str[0]
    split_allele = risk_allele_str.str.split('-', n=1, expand=True)
    results_df['variant_rsID'] = split_allele[0]
    
    # Set pandas display format for p-values
    pd.options.display.float_format = '{:.2e}'.format
    
    display(results_df[[
        'p_value',
        'variant_rsID',
        'initial_sample_size', # Display the sample size
        'accession_id'
    ]])
    
    pd.reset_option('display.float_format') # Reset display format
else:
    print(f"Could not retrieve top associations for '{trait_of_interest}'.")
```

    --- Step 1: Finding the top 10 most significant associations for 'type 2 diabetes mellitus' ---
    Found 10 associations. Now fetching their study sample sizes...
    
    --- Search Complete ---



<div>
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>p_value</th>
      <th>variant_rsID</th>
      <th>initial_sample_size</th>
      <th>accession_id</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>50,251 African American cases, 103,909 African...</td>
      <td>GCST90492734</td>
    </tr>
    <tr>
      <th>1</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>148,726 European ancestry cases, 965,732 Europ...</td>
      <td>GCST010555</td>
    </tr>
    <tr>
      <th>2</th>
      <td>0.00e+00</td>
      <td>rs35011184</td>
      <td>148,726 European ancestry cases, 24,646 Africa...</td>
      <td>GCST010557</td>
    </tr>
    <tr>
      <th>3</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>51,256 African, African American, East Asian, ...</td>
      <td>GCST90444202</td>
    </tr>
    <tr>
      <th>4</th>
      <td>0.00e+00</td>
      <td>rs2237897</td>
      <td>50,251 African American cases, 103,909 African...</td>
      <td>GCST90492734</td>
    </tr>
    <tr>
      <th>5</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>251,740 European ancestry individuals, 139,705...</td>
      <td>GCST90132183</td>
    </tr>
    <tr>
      <th>6</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>74,124 European ancestry cases, 824,006 Europe...</td>
      <td>GCST009379</td>
    </tr>
    <tr>
      <th>7</th>
      <td>0.00e+00</td>
      <td>rs7766070</td>
      <td>50,251 African American cases, 103,909 African...</td>
      <td>GCST90492734</td>
    </tr>
    <tr>
      <th>8</th>
      <td>0.00e+00</td>
      <td>rs10811661</td>
      <td>50,251 African American cases, 103,909 African...</td>
      <td>GCST90492734</td>
    </tr>
    <tr>
      <th>9</th>
      <td>0.00e+00</td>
      <td>rs7903146</td>
      <td>61,714 European ancestry cases, 1,178 Pakistan...</td>
      <td>GCST006867</td>
    </tr>
  </tbody>
</table>
</div>



```python

```
