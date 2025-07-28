```python
# Install necessary libraries
!pip install requests pandas
```

    Requirement already satisfied: requests in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (2.32.4)
    Requirement already satisfied: pandas in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (2.3.1)
    Requirement already satisfied: charset_normalizer<4,>=2 in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (from requests) (3.4.2)
    Requirement already satisfied: idna<4,>=2.5 in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (from requests) (3.10)
    Requirement already satisfied: urllib3<3,>=1.21.1 in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (from requests) (2.5.0)
    Requirement already satisfied: certifi>=2017.4.17 in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (from requests) (2025.7.14)
    Requirement already satisfied: numpy>=1.26.0 in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (from pandas) (2.3.1)
    Requirement already satisfied: python-dateutil>=2.8.2 in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (from pandas) (2.9.0.post0)
    Requirement already satisfied: pytz>=2020.1 in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (from pandas) (2025.2)
    Requirement already satisfied: tzdata>=2022.7 in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (from pandas) (2025.2)
    Requirement already satisfied: six>=1.5 in /Users/karatugo/Documents/GitHub/gwas-rest-api/docs/venv/lib/python3.13/site-packages (from python-dateutil>=2.8.2->pandas) (1.17.0)



```python
# Import libraries
import requests
import pandas as pd
import json
```


```python
# Set the base URL for the GWAS Catalog REST API
BASE_URL = "https://wwwdev.ebi.ac.uk/gwas/beta/rest/api"

# Set pandas display options to show all columns
pd.set_option('display.max_columns', None)

print("Setup complete. Libraries are installed and imported.")
```

    Setup complete. Libraries are installed and imported.



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
                print(f"Found {data['page']['totalElements']} total associations across {total_pages} pages.")

            # Extract the rsID from each association
            for association in associations_list:
                if 'snp_risk_allele' in association and association['snp_risk_allele']:
                    # e.g., from ['rs123-A'], get 'rs123'
                    risk_allele_str = association['snp_risk_allele'][0]
                    rs_id = risk_allele_str.split('-')[0]
                    if rs_id.startswith('rs'):
                        variants.add(rs_id)
            
            print(f"Page {current_page + 1}/{total_pages} processed. Found {len(variants)} unique variants so far.")
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
                print(f"Found {associations_data['page']['totalElements']} total associations across {total_pages} pages.")
    
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
    
            print(f"Processed page {current_page + 1} of {total_pages}. Found {len(all_genes)} unique genes so far.")
            current_page += 1
        else:
            # Stop if there's no more data or an error occurs
            print("No more data found or an error occurred. Stopping.")
            break

        return all_genes
```


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


### Question 1: What variants are associated with a specific disease, e.g. type 2 diabetes?


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
    
    # Parse the 'snp_risk_allele' field. It's a list containing a string like 'rsID-Allele'
    # Extract the string 'rsID-Allele' from the list
    risk_allele_str = associations_df['snp_risk_allele'].str[0]
    
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
    ]]

    display(display_df)
else:
    print(f"No associations found for '{disease_of_interest}' or an error occurred.")

```

    Found 7651 variants associated with 'type 2 diabetes mellitus'.
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
      <td>rs16989540</td>
      <td>C</td>
      <td>1.000000e-11</td>
      <td>0.874914</td>
      <td>GCST90528074</td>
      <td>[DEPDC5]</td>
    </tr>
    <tr>
      <th>1</th>
      <td>rs58223766</td>
      <td>C</td>
      <td>3.000000e-09</td>
      <td>0.796662</td>
      <td>GCST90528074</td>
      <td>[SLC16A13, SLC16A11]</td>
    </tr>
    <tr>
      <th>2</th>
      <td>rs1421085</td>
      <td>T</td>
      <td>1.000000e-13</td>
      <td>0.750703</td>
      <td>GCST90528074</td>
      <td>[FTO]</td>
    </tr>
    <tr>
      <th>3</th>
      <td>rs10830963</td>
      <td>C</td>
      <td>5.000000e-11</td>
      <td>0.78925</td>
      <td>GCST90528074</td>
      <td>[MTNR1B]</td>
    </tr>
    <tr>
      <th>4</th>
      <td>rs79878066</td>
      <td>C</td>
      <td>2.000000e-41</td>
      <td>0.792611</td>
      <td>GCST90528074</td>
      <td>[KCNQ1]</td>
    </tr>
    <tr>
      <th>5</th>
      <td>rs34872471</td>
      <td>T</td>
      <td>3.000000e-46</td>
      <td>0.739357</td>
      <td>GCST90528074</td>
      <td>[TCF7L2]</td>
    </tr>
    <tr>
      <th>6</th>
      <td>rs10882099</td>
      <td>T</td>
      <td>8.000000e-11</td>
      <td>0.645401</td>
      <td>GCST90528074</td>
      <td>[HHEX, Y_RNA]</td>
    </tr>
    <tr>
      <th>7</th>
      <td>rs12780155</td>
      <td>T</td>
      <td>7.000000e-11</td>
      <td>0.758083</td>
      <td>GCST90528074</td>
      <td>[CDC123]</td>
    </tr>
    <tr>
      <th>8</th>
      <td>rs12344703</td>
      <td>T</td>
      <td>2.000000e-08</td>
      <td>0.787072</td>
      <td>GCST90528074</td>
      <td>[MOB3B]</td>
    </tr>
    <tr>
      <th>9</th>
      <td>rs12555274</td>
      <td>G</td>
      <td>7.000000e-10</td>
      <td>0.711345</td>
      <td>GCST90528074</td>
      <td>[CDKN2B-AS1]</td>
    </tr>
  </tbody>
</table>
</div>


### Question 2: What studies are available for "type 2 diabetes" in the "UKB" cohort?

To answer this, we'll use the `/v2/studies` endpoint. This endpoint can be filtered by multiple criteria simultaneously. We will provide both the `disease_trait` ("type 2 diabetes") and the `cohort` ("UKB") as parameters to narrow down the results.


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

    Found 8 studies for 'type 2 diabetes' in the 'UKB' cohort.
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
      <td>GCST90444202</td>
      <td>39379762</td>
      <td>Type 2 diabetes</td>
      <td>51,256 African, African American, East Asian, ...</td>
      <td>[UKB, MGBB, GERA, AllofUs]</td>
    </tr>
    <tr>
      <th>1</th>
      <td>GCST90302887</td>
      <td>37377600</td>
      <td>Type 2 diabetes</td>
      <td>22,670 British ancestry cases, 313,404 British...</td>
      <td>[UKB]</td>
    </tr>
    <tr>
      <th>2</th>
      <td>GCST90077724</td>
      <td>34662886</td>
      <td>Type 2 diabetes</td>
      <td>3,497 European ancestry cases, 328,257 Europea...</td>
      <td>[UKB]</td>
    </tr>
    <tr>
      <th>3</th>
      <td>GCST90132186</td>
      <td>35551307</td>
      <td>Type 2 diabetes</td>
      <td>40,737 South Asian ancestry individuals</td>
      <td>[Other, ITH, LOLIPOP, PROMIS, RHS, SINDI, UKB]</td>
    </tr>
    <tr>
      <th>4</th>
      <td>GCST90132184</td>
      <td>35551307</td>
      <td>Type 2 diabetes</td>
      <td>251,740 European ancestry individuals</td>
      <td>[BIOME, DECODE, DGDG, DGI, EGCUT, EPIC, FHS, F...</td>
    </tr>
    <tr>
      <th>5</th>
      <td>GCST90100587</td>
      <td>34862199</td>
      <td>Type 2 diabetes</td>
      <td>33,139 European ancestry cases, 279,507 Europe...</td>
      <td>[UKB, FUSION, WTCCC, GERA, MGBB, other]</td>
    </tr>
    <tr>
      <th>6</th>
      <td>GCST90018926</td>
      <td>34594039</td>
      <td>Type 2 diabetes</td>
      <td>38,841 European ancestry cases, 451,248 Europe...</td>
      <td>[BBJ, UKB, FinnGen]</td>
    </tr>
    <tr>
      <th>7</th>
      <td>GCST90038634</td>
      <td>33959723</td>
      <td>Type 2 diabetes</td>
      <td>3,260 cases, 481,338 controls</td>
      <td>[UKB]</td>
    </tr>
  </tbody>
</table>
</div>


### Question 3: What are the most significant associations for a specific SNP? eg: significant associations for rs1050316

To answer this, we will again use the `/v2/associations` endpoint. This time, we'll filter using the `rs_id` parameter to specify our variant of interest.

To find the **most significant** results, we will instruct the API to sort the data by `p_value` in `asc` (ascending) order. This puts the associations with the smallest p-values (highest significance) at the top of the list.


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

To answer this, there isn't a direct endpoint to look up genes by disease. Instead, we can find the answer by:
1. Fetching all the associations for a given disease (e.g., "type 2 diabetes").
2. Extracting the mapped_genes from each of those associations.
3. Because there can be thousands of associations for a common disease, we will need to loop through multiple pages of API results to build a comprehensive list.


```python
# The disease we want to find associated genes for
disease_of_interest = "type 2 diabetes mellitus"
print(f"Searching for genes associated with '{disease_of_interest}'...")

# # Use a set to store unique gene names automatically
# all_genes = set()
# current_page = 0
# total_pages = 1 # Initialize to 1 to start the loop

# # Loop through all pages of the API results
# while current_page < total_pages:
#     # Define the endpoint and parameters
#     endpoint = "/v2/associations"
#     params = {
#         "efo_trait": disease_of_interest,
#         "size": 200,  # Request a larger size per page for efficiency
#         "page": current_page
#     }

#     # Make the API call
#     associations_data = gwas_api_request(endpoint, params)

#     # Check if the request was successful and contains data
#     if associations_data and '_embedded' in associations_data:
#         # Extract the list of associations
#         associations_list = associations_data['_embedded']['associations']
        
#         # Update the total number of pages from the first response
#         if current_page == 0:
#             total_pages = associations_data['page']['totalPages']
#             print(f"Found {associations_data['page']['totalElements']} total associations across {total_pages} pages.")

#         # Process each association in the current page
#         for association in associations_list:
#             # Check if 'mapped_genes' exists and is not empty
#             if 'mapped_genes' in association and association['mapped_genes']:
#                 # The field is a list, e.g., ['GENE1', 'GENE2,GENE3']
#                 for gene_string in association['mapped_genes']:
#                     # A single string can have multiple comma-separated genes
#                     genes = gene_string.split(',')
#                     for gene in genes:
#                         if gene.strip():  # Ensure the gene name isn't empty
#                             all_genes.add(gene.strip())

#         print(f"Processed page {current_page + 1} of {total_pages}. Found {len(all_genes)} unique genes so far.")
#         current_page += 1
#     else:
#         # Stop if there's no more data or an error occurs
#         print("No more data found or an error occurred. Stopping.")
#         break

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
    Found 7651 total associations across 39 pages.
    Processed page 1 of 39. Found 226 unique genes so far.
    
    --- Search Complete ---
    Found a total of 226 unique genes associated with 'type 2 diabetes mellitus'.
    Example genes: ['ABHD17A', 'ACBD4', 'ACE', 'ADA', 'ADARB1', 'ADCY5', 'AIDAP3', 'ANGPTL4', 'APOE', 'ARSG', 'ARVCF', 'ASCC2', 'ASXL1', 'ATP1B2', 'ATP2A3', 'ATP5F1E', 'ATXN10', 'BACE2', 'BBLNP1', 'BCL2', 'BPTF', 'CBARP', 'CBARP-DT', 'CBFA2T2', 'CBX1', 'CDC123', 'CDH7', 'CDK12', 'CDKAL1', 'CDKN2B-AS1', 'CEBPB', 'CHMP4B', 'CHRNE', 'CLEC10A', 'CRTC1', 'CYB5R3', 'CYCSP55', 'CYTH1', 'DCAF7', 'DCC', 'DDX52', 'DEPDC5', 'DPEP1', 'DTD1', 'EMILIN2', 'EP300', 'EP300-AS1', 'ERN1', 'ETS2', 'EXOC3L2', 'EYA2', 'FARSA', 'FBXO46', 'FCGRT', 'FN3KRP', 'FTO', 'GALK1', 'GIP', 'GIPR', 'GLIS3', 'GLP2R', 'GNAS', 'GNAS-AS1', 'GRP', 'HHEX', 'HMGA1', 'HNF1B', 'HNF4A', 'HNF4A-AS1', 'HSD17B1', 'HSD17B1-AS1', 'IGF2BP1', 'IGF2BP2', 'INSR', 'ITGA3', 'JAZF1', 'KANSL1', 'KCNJ12', 'KCNQ1', 'KDM4B', 'KLB', 'KLF16', 'LAMA1', 'LAMA5', 'LIME1', 'LINC00114', 'LINC00261', 'LINC00470', 'LINC00511', 'LINC00907', 'LINC00910', 'LINC01415', 'LINC01429', 'LINC01440', 'LINC01524', 'LINC01893', 'LINC01898', 'LINC01978', 'LINC01979', 'LINC03111']


### Question 5: What are the top significant SNPs for type 2 diabetes in the European population?

To answer this, we need to combine data from two different endpoints: `/v2/associations` and `/v2/studies`.

1.  First, we'll get a list of all associations for "type 2 diabetes", sorted by p-value.
2.  Then, for each of those associations, we will look up its study using the `/v2/studies/{accession_id}` endpoint.
3.  We will check the study's `discovery_ancestry` field to see if it matches "European".
4.  We will stop once we have found the top 10 associations that meet this ancestry criteria.


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
    risk_allele_str = results_df['snp_risk_allele'].str[0]
    split_allele = risk_allele_str.str.split('-', n=1, expand=True)
    results_df['variant_rsID'] = split_allele[0]
    results_df['risk_allele_base'] = split_allele[1]
    
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
    Error: Received status code 500
    URL: https://wwwdev.ebi.ac.uk/gwas/beta/rest/api/v2/associations?efo_trait=type+2+diabetes+mellitus&sort=p_value&direction=asc&size=200&page=0
    Response: {"timestamp":"2025-07-25T15:20:01.990+00:00","status":500,"error":"Internal Server Error","path":"/gwas/beta/rest/api/v2/associations"}
    No more data or an error occurred. Stopping.
    --- Finished fetching. Found 0 total associations. ---
    
    
    --- Search Complete ---
    Could not find any matching associations.


### Question 6: Which variants are common between "type 2 diabetes" and "obesity"?

To find the variants associated with both traits, we'll follow these steps:

1.  Get all unique variants for "type 2 diabetes".
2.  Get all unique variants for "obesity".
3.  Compare the two sets of variants to find the ones they have in common.


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
    Found 7651 total associations across 39 pages.
    Page 1/39 processed. Found 200 unique variants so far.
    Page 2/39 processed. Found 400 unique variants so far.
    Page 3/39 processed. Found 599 unique variants so far.
    Page 4/39 processed. Found 798 unique variants so far.
    Page 5/39 processed. Found 998 unique variants so far.
    Page 6/39 processed. Found 1198 unique variants so far.
    Page 7/39 processed. Found 1369 unique variants so far.
    Page 8/39 processed. Found 1416 unique variants so far.
    Page 9/39 processed. Found 1464 unique variants so far.
    Page 10/39 processed. Found 1617 unique variants so far.
    Page 11/39 processed. Found 1759 unique variants so far.
    Page 12/39 processed. Found 1951 unique variants so far.
    Page 13/39 processed. Found 2124 unique variants so far.
    Page 14/39 processed. Found 2189 unique variants so far.
    Page 15/39 processed. Found 2264 unique variants so far.
    Page 16/39 processed. Found 2279 unique variants so far.
    Page 17/39 processed. Found 2294 unique variants so far.
    Page 18/39 processed. Found 2401 unique variants so far.
    Error: Received status code 500
    URL: https://wwwdev.ebi.ac.uk/gwas/beta/rest/api/v2/associations?efo_trait=type+2+diabetes+mellitus&size=200&page=18
    Response: {"timestamp":"2025-07-25T15:22:00.759+00:00","status":500,"error":"Internal Server Error","path":"/gwas/beta/rest/api/v2/associations"}
    No more data or an error occurred. Stopping.
    --- Finished fetching for 'type 2 diabetes mellitus'. Found 2401 total unique variants. ---
    
    --- Starting search for 'obesity' ---
    Found 614 total associations across 4 pages.
    Page 1/4 processed. Found 104 unique variants so far.
    Page 2/4 processed. Found 174 unique variants so far.
    Error: Received status code 500
    URL: https://wwwdev.ebi.ac.uk/gwas/beta/rest/api/v2/associations?efo_trait=obesity&size=200&page=2
    Response: {"timestamp":"2025-07-25T15:22:16.297+00:00","status":500,"error":"Internal Server Error","path":"/gwas/beta/rest/api/v2/associations"}
    No more data or an error occurred. Stopping.
    --- Finished fetching for 'obesity'. Found 174 total unique variants. ---
    
    
    --- Search Complete ---
    Found 26 variants common to both 'type 2 diabetes mellitus' and 'obesity':



    ['rs11642015',
     'rs1229984',
     'rs1296328',
     'rs13028310',
     'rs13130484',
     'rs1412265',
     'rs1421085',
     'rs1556036',
     'rs17770336',
     'rs1955851',
     'rs2239647',
     'rs2306593',
     'rs2307111',
     'rs261966',
     'rs34811474',
     'rs34872471',
     'rs429358',
     'rs4776970',
     'rs4923464',
     'rs539515',
     'rs56094641',
     'rs6567160',
     'rs7132908',
     'rs7498798',
     'rs7903146',
     'rs8050136']


### Question 7: What are the most reported genes for a specific disease?

1. Get all associations for the trait
2. Extract and count all gene mentions
3. Display the top N most common genes


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
    Error: Received status code 500
    URL: https://wwwdev.ebi.ac.uk/gwas/beta/rest/api/v2/associations?efo_trait=type+2+diabetes+mellitus&sort=p_value&direction=asc&size=200&page=0
    Response: {"timestamp":"2025-07-25T15:22:18.033+00:00","status":500,"error":"Internal Server Error","path":"/gwas/beta/rest/api/v2/associations"}
    No more data or an error occurred. Stopping.
    --- Finished fetching. Found 0 total associations. ---
    
    
    --- Analysis Complete ---
    Could not find any gene associations for 'type 2 diabetes mellitus'.


### Question 8: What traits are associated with a particular SNP?

1. Paginate through all associations for the SNP
2. Extract the trait name from each association


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

1. Paginate through all matching studies
2. Display the final results


```python
trait_of_interest = "type 2 diabetes"
studies_with_sum_stats = [] # A list to store the resulting study objects

print(f"Searching for studies on '{trait_of_interest}' with full summary statistics...")

# --- Step 1: Paginate through all matching studies ---
current_page = 0
total_pages = 1 # Initialize to 1 to start the loop

while current_page < total_pages:
    endpoint = "/v2/studies"
    params = {
        "disease_trait": trait_of_interest,
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

    Searching for studies on 'type 2 diabetes' with full summary statistics...
    Found 32 total matching studies across 1 pages.
    Page 1/1 processed. Collected 32 studies so far.
    
    --- Search Complete ---
    Found a total of 32 studies for 'type 2 diabetes' with full summary statistics.



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
      <td>GCST90528074</td>
      <td>40210677</td>
      <td>23,541 Hispanic or Latino cases, 37,434 Hispan...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>1</th>
      <td>GCST90444202</td>
      <td>39379762</td>
      <td>51,256 African, African American, East Asian, ...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>2</th>
      <td>GCST90302887</td>
      <td>37377600</td>
      <td>22,670 British ancestry cases, 313,404 British...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>3</th>
      <td>GCST90077724</td>
      <td>34662886</td>
      <td>3,497 European ancestry cases, 328,257 Europea...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>4</th>
      <td>GCST90296698</td>
      <td>38182742</td>
      <td>21,507 Finnish ancestry female cases, 156,472 ...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>5</th>
      <td>GCST90296697</td>
      <td>38182742</td>
      <td>27,607 Finnish ancestry male cases, 118,687 Fi...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>6</th>
      <td>GCST90161239</td>
      <td>36329257</td>
      <td>3,844 Taiwanese ancestry cases, 59,333 Taiwane...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>7</th>
      <td>GCST90006934</td>
      <td>33188205</td>
      <td>9,978 European ancestry cases, 12,348 European...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>8</th>
      <td>GCST90018926</td>
      <td>34594039</td>
      <td>38,841 European ancestry cases, 451,248 Europe...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>9</th>
      <td>GCST90018706</td>
      <td>34594039</td>
      <td>45,383 East Asian ancestry cases, 132,032 East...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>10</th>
      <td>GCST90086072</td>
      <td>33893285</td>
      <td>6,967 European ancestry cases, 49,670 European...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>11</th>
      <td>GCST90086071</td>
      <td>33893285</td>
      <td>6,967 European ancestry cases, 49,670 European...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>12</th>
      <td>GCST90086070</td>
      <td>33893285</td>
      <td>6,967 European ancestry cases, 49,670 European...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>13</th>
      <td>GCST90086069</td>
      <td>33893285</td>
      <td>6,967 European ancestry cases, 49,670 European...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>14</th>
      <td>GCST90086068</td>
      <td>33893285</td>
      <td>6,967 European ancestry cases, 49,670 European...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>15</th>
      <td>GCST90026417</td>
      <td>34737425</td>
      <td>9,486 Scandinavian ancestry cases, 2,744 Scand...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>16</th>
      <td>GCST90038634</td>
      <td>33959723</td>
      <td>3,260 cases, 481,338 controls</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>17</th>
      <td>GCST90029024</td>
      <td>29892013</td>
      <td>468,298 European ancestry individuals</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>18</th>
      <td>GCST90012181</td>
      <td>32915819</td>
      <td>2,824 German ancestry individuals</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>19</th>
      <td>GCST010118</td>
      <td>32499647</td>
      <td>77,418 East Asian cases, 356,122 East Asian co...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>20</th>
      <td>GCST008048</td>
      <td>31217584</td>
      <td>5,971 African American cases, 4,135 Hispanic/L...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>21</th>
      <td>GCST008114</td>
      <td>31049640</td>
      <td>2,633 African ancestry cases, 1,714 African an...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>22</th>
      <td>GCST007517</td>
      <td>29632382</td>
      <td>up to 48,286 European ancestry cases, up to 25...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>23</th>
      <td>GCST007847</td>
      <td>30718926</td>
      <td>36,614 Japanese ancestry cases, 155,150 Japane...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>24</th>
      <td>GCST006801</td>
      <td>26961502</td>
      <td>4,040 British ancestry cases, 113,735 British ...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>25</th>
      <td>GCST006867</td>
      <td>30054458</td>
      <td>61,714 European ancestry cases, 1,178 Pakistan...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>26</th>
      <td>GCST007515</td>
      <td>29632382</td>
      <td>48,286 European ancestry cases, 250,671 Europe...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>27</th>
      <td>GCST005898</td>
      <td>29358691</td>
      <td>5,277 European ancestry cases, 15,702 European...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>28</th>
      <td>GCST005413</td>
      <td>29358691</td>
      <td>up to 12,931 European ancestry cases, up to 57...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>29</th>
      <td>GCST005047</td>
      <td>22885922</td>
      <td>6,377 European ancestry male cases, 5,794 Euro...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>30</th>
      <td>GCST004773</td>
      <td>28566273</td>
      <td>up to 26,676 European ancestry cases, up to 13...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
    <tr>
      <th>31</th>
      <td>GCST000028</td>
      <td>17463246</td>
      <td>1,464 European ancestry cases, 1,467 European ...</td>
      <td>http://ftp.ebi.ac.uk/pub/databases/gwas/summar...</td>
    </tr>
  </tbody>
</table>
</div>


### Question 10: What variants are in a gene `HBS1L` and what are their associated traits?

1. We'll use the `/v2/single-nucleotide-polymorphisms` endpoint to find all variants (SNPs) mapped to our gene of interest.

2. For each of those variants, we'll query the `/v2/associations` endpoint to find all the traits linked to it.


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
    params = {"gene": gene_of_interest, "size": 20, "page": current_page}
    data = gwas_api_request(endpoint, params)
    
    if data and '_embedded' in data:
        snp_list = data['_embedded']['snps']
        for snp in snp_list:
            if 'rs_id' in snp:
                gene_variants_rsids.append(snp['rs_id'])
        
        if current_page == 0:
            total_pages = data['page']['totalPages']
        print(f"Page {current_page + 1}/{total_pages} processed. Found {len(gene_variants_rsids)} variants so far.")
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
    print(f"Found {len(traits_for_snp)} traits for {rs_id}.")
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
    Page 1/20754 processed. Found 20 variants so far.
    Page 2/20754 processed. Found 40 variants so far.
    Page 3/20754 processed. Found 60 variants so far.
    Page 4/20754 processed. Found 80 variants so far.
    Page 5/20754 processed. Found 100 variants so far.
    Page 6/20754 processed. Found 120 variants so far.
    Page 7/20754 processed. Found 140 variants so far.
    Page 8/20754 processed. Found 160 variants so far.
    Page 9/20754 processed. Found 180 variants so far.
    Page 10/20754 processed. Found 200 variants so far.
    Page 11/20754 processed. Found 220 variants so far.
    Page 12/20754 processed. Found 240 variants so far.
    Page 13/20754 processed. Found 260 variants so far.
    Page 14/20754 processed. Found 280 variants so far.
    Page 15/20754 processed. Found 300 variants so far.
    Page 16/20754 processed. Found 320 variants so far.
    Page 17/20754 processed. Found 340 variants so far.
    Page 18/20754 processed. Found 360 variants so far.
    Page 19/20754 processed. Found 380 variants so far.
    Page 20/20754 processed. Found 400 variants so far.
    Page 21/20754 processed. Found 420 variants so far.
    Page 22/20754 processed. Found 440 variants so far.
    Page 23/20754 processed. Found 460 variants so far.
    Page 24/20754 processed. Found 480 variants so far.
    Page 25/20754 processed. Found 500 variants so far.
    Page 26/20754 processed. Found 520 variants so far.
    Page 27/20754 processed. Found 540 variants so far.
    Page 28/20754 processed. Found 560 variants so far.
    Page 29/20754 processed. Found 580 variants so far.
    Page 30/20754 processed. Found 600 variants so far.
    Page 31/20754 processed. Found 620 variants so far.
    Page 32/20754 processed. Found 640 variants so far.
    Page 33/20754 processed. Found 660 variants so far.
    Page 34/20754 processed. Found 680 variants so far.
    Page 35/20754 processed. Found 700 variants so far.
    Page 36/20754 processed. Found 720 variants so far.
    Page 37/20754 processed. Found 740 variants so far.
    Page 38/20754 processed. Found 760 variants so far.
    Page 39/20754 processed. Found 780 variants so far.
    Page 40/20754 processed. Found 800 variants so far.
    Page 41/20754 processed. Found 820 variants so far.
    Page 42/20754 processed. Found 840 variants so far.
    Page 43/20754 processed. Found 860 variants so far.
    Page 44/20754 processed. Found 880 variants so far.
    Page 45/20754 processed. Found 900 variants so far.
    Page 46/20754 processed. Found 920 variants so far.
    Page 47/20754 processed. Found 940 variants so far.
    Page 48/20754 processed. Found 960 variants so far.
    Page 49/20754 processed. Found 980 variants so far.
    Page 50/20754 processed. Found 1000 variants so far.
    Page 51/20754 processed. Found 1020 variants so far.
    Page 52/20754 processed. Found 1040 variants so far.
    Page 53/20754 processed. Found 1060 variants so far.
    Page 54/20754 processed. Found 1080 variants so far.
    Page 55/20754 processed. Found 1100 variants so far.
    Page 56/20754 processed. Found 1120 variants so far.
    Page 57/20754 processed. Found 1140 variants so far.
    Page 58/20754 processed. Found 1160 variants so far.
    Page 59/20754 processed. Found 1180 variants so far.
    Page 60/20754 processed. Found 1200 variants so far.
    Page 61/20754 processed. Found 1220 variants so far.
    Page 62/20754 processed. Found 1240 variants so far.
    Page 63/20754 processed. Found 1260 variants so far.
    Page 64/20754 processed. Found 1280 variants so far.
    Page 65/20754 processed. Found 1300 variants so far.
    Page 66/20754 processed. Found 1320 variants so far.
    Page 67/20754 processed. Found 1340 variants so far.
    Page 68/20754 processed. Found 1360 variants so far.
    Page 69/20754 processed. Found 1380 variants so far.
    Page 70/20754 processed. Found 1400 variants so far.
    Page 71/20754 processed. Found 1420 variants so far.
    Page 72/20754 processed. Found 1440 variants so far.
    Page 73/20754 processed. Found 1460 variants so far.
    Page 74/20754 processed. Found 1480 variants so far.
    Page 75/20754 processed. Found 1500 variants so far.
    Page 76/20754 processed. Found 1520 variants so far.
    Page 77/20754 processed. Found 1540 variants so far.
    Page 78/20754 processed. Found 1560 variants so far.
    Page 79/20754 processed. Found 1580 variants so far.
    Page 80/20754 processed. Found 1600 variants so far.
    Page 81/20754 processed. Found 1620 variants so far.
    Page 82/20754 processed. Found 1640 variants so far.
    Page 83/20754 processed. Found 1660 variants so far.
    Page 84/20754 processed. Found 1680 variants so far.
    Page 85/20754 processed. Found 1700 variants so far.
    Page 86/20754 processed. Found 1720 variants so far.
    Page 87/20754 processed. Found 1740 variants so far.
    Page 88/20754 processed. Found 1760 variants so far.
    Page 89/20754 processed. Found 1780 variants so far.
    Page 90/20754 processed. Found 1800 variants so far.
    Page 91/20754 processed. Found 1820 variants so far.
    Page 92/20754 processed. Found 1840 variants so far.
    Page 93/20754 processed. Found 1860 variants so far.
    Page 94/20754 processed. Found 1880 variants so far.
    Page 95/20754 processed. Found 1900 variants so far.
    Page 96/20754 processed. Found 1920 variants so far.
    Page 97/20754 processed. Found 1940 variants so far.
    Page 98/20754 processed. Found 1960 variants so far.
    Page 99/20754 processed. Found 1980 variants so far.
    Page 100/20754 processed. Found 2000 variants so far.
    Page 101/20754 processed. Found 2020 variants so far.
    Page 102/20754 processed. Found 2040 variants so far.
    Page 103/20754 processed. Found 2060 variants so far.
    Page 104/20754 processed. Found 2080 variants so far.
    Page 105/20754 processed. Found 2100 variants so far.
    Page 106/20754 processed. Found 2120 variants so far.
    Page 107/20754 processed. Found 2140 variants so far.
    Page 108/20754 processed. Found 2160 variants so far.
    Page 109/20754 processed. Found 2180 variants so far.
    Page 110/20754 processed. Found 2200 variants so far.
    Page 111/20754 processed. Found 2220 variants so far.
    Page 112/20754 processed. Found 2240 variants so far.
    Error: Received status code 500
    URL: https://wwwdev.ebi.ac.uk/gwas/beta/rest/api/v2/single-nucleotide-polymorphisms?gene=HBS1L&size=20&page=112
    Response: {"timestamp":"2025-07-25T15:30:54.106+00:00","status":500,"error":"Internal Server Error","path":"/gwas/beta/rest/api/v2/single-nucleotide-polymorphisms"}
    
    Found 2240 total variants for 'HBS1L'.
    
    --- Step 2: Checking for associated traits for the first 2240 variants ---
    Found 1 traits for rs117741236.
    Found 1 traits for rs1853229.
    Found 1 traits for rs138801403.
    Found 1 traits for rs56289435.
    Found 1 traits for rs4786119.
    Found 1 traits for rs1075046.
    Found 1 traits for rs9348718.
    Found 1 traits for rs918518.
    Found 1 traits for rs7789481.
    Found 1 traits for rs6918223.
    Found 1 traits for rs6916186.
    Found 1 traits for rs62443225.
    Found 1 traits for rs62401434.
    Found 1 traits for rs6067293.
    Found 1 traits for rs583522.
    Found 1 traits for rs34693947.
    Found 1 traits for rs2596546.
    Found 1 traits for rs1306395.
    Found 1 traits for rs11575232.
    Found 2 traits for rs35354956.
    Found 2 traits for rs2967776.
    Found 3 traits for rs11833002.
    Found 2 traits for rs7393121.
    Found 2 traits for rs3735817.
    Found 2 traits for rs142378953.
    Found 2 traits for rs116989816.
    Found 2 traits for rs12928070.
    Found 2 traits for rs142862631.
    Found 2 traits for chr8:63589187.
    Found 2 traits for rs76796948.
    Found 2 traits for rs9313327.
    Found 2 traits for rs187968973.
    Found 2 traits for rs13126197.
    Found 2 traits for rs55872473.
    Found 2 traits for rs73988137.
    Found 2 traits for rs17032116.
    Found 2 traits for rs115206839.
    Found 2 traits for rs77945679.
    Found 2 traits for rs115828022.
    Found 2 traits for rs28529330.
    Found 2 traits for rs11949029.
    Found 2 traits for rs34636484.
    Found 2 traits for rs4553811.
    Found 2 traits for rs2076148.
    Found 2 traits for rs2421154.
    Found 2 traits for rs2241342.
    Found 2 traits for chr11:123152496.
    Found 2 traits for rs78982693.
    Found 2 traits for rs116905874.
    Found 2 traits for rs114846502.
    Found 2 traits for rs7124955.
    Found 2 traits for rs11702544.
    Found 2 traits for rs1373068.
    Found 2 traits for rs1672875.
    Found 2 traits for rs2691595.
    Found 2 traits for rs115235881.
    Found 2 traits for rs2972157.
    Found 2 traits for chr15:91379070.
    Found 2 traits for rs16987300.
    Found 2 traits for rs78030282.
    Found 2 traits for rs75584073.
    Found 2 traits for rs77223107.
    Found 2 traits for rs11014479.
    Found 2 traits for rs72979578.
    Found 2 traits for rs147432166.
    Found 2 traits for rs140292606.
    Found 2 traits for rs28392483.
    Found 2 traits for rs1165225.
    Found 2 traits for chr19:11341680.
    Found 2 traits for rs75728735.
    Found 2 traits for rs111315946.
    Found 3 traits for rs191496493.
    Found 2 traits for rs3779787.
    Found 2 traits for rs11592067.
    Found 2 traits for rs138175288.
    Found 2 traits for rs79095752.
    Found 2 traits for rs116786842.
    Found 2 traits for rs12667186.
    Found 2 traits for rs118064778.
    Found 2 traits for rs398227.
    Found 2 traits for rs13336189.
    Found 2 traits for rs2918952.
    Found 2 traits for chr15:54249794.
    Found 2 traits for rs9977076.
    Found 2 traits for rs10404380.
    Found 2 traits for rs8054091.
    Found 2 traits for rs13233731.
    Found 2 traits for rs6978097.
    Found 3 traits for rs72640234.
    Found 3 traits for rs17111648.
    Found 2 traits for rs4474673.
    Found 2 traits for rs73597733.
    Found 2 traits for rs6985493.
    Found 2 traits for rs77775955.
    Found 2 traits for rs116515271.
    Found 2 traits for rs78061159.
    Found 2 traits for rs11083637.
    Found 2 traits for rs113389818.
    Found 2 traits for rs111970601.
    Found 2 traits for rs796841295.
    Found 2 traits for rs9836522.
    Found 2 traits for chr14:34945505.
    Found 2 traits for rs1400463140.
    Found 2 traits for rs150874134.
    Found 2 traits for rs148939577.
    Found 2 traits for rs146585356.
    Found 2 traits for rs145845084.
    Found 2 traits for rs142792919.
    Found 2 traits for rs9657704.
    Found 2 traits for rs2078778.
    Found 2 traits for rs150430050.
    Found 2 traits for rs148398679.
    Found 2 traits for rs145587141.
    Found 2 traits for rs140803269.
    Found 2 traits for rs12380892.
    Found 2 traits for rs10993918.
    Found 2 traits for rs10993905.
    Found 2 traits for rs10821555.
    Found 2 traits for rs10821554.
    Found 2 traits for rs10821552.
    Found 2 traits for rs10821545.
    Found 1 traits for rs11707457.
    Found 1 traits for chr13:21440496.
    Found 1 traits for chr3:49771990.
    Found 1 traits for rs2062848.
    Found 1 traits for rs3744825.
    Found 1 traits for rs9292705.
    Found 1 traits for rs77355897.
    Found 1 traits for rs1727491.
    Found 1 traits for rs1909357.
    Found 1 traits for rs2849841.
    Found 1 traits for rs10116741.
    Found 1 traits for rs35668634.
    Found 1 traits for rs2977296.
    Found 1 traits for rs1825713.
    Found 1 traits for rs11756472.
    Found 1 traits for rs62360439.
    Found 1 traits for rs12664349.
    Found 1 traits for rs8072716.
    Found 1 traits for rs7862032.
    Found 1 traits for rs7640987.
    Found 1 traits for rs7935771.
    Found 1 traits for rs9384964.
    Found 1 traits for rs6466817.
    Found 2 traits for rs4865143.
    Found 1 traits for rs3847795.
    Found 1 traits for rs56024259.
    Found 1 traits for rs6905617.
    Found 1 traits for rs2413206.
    Found 1 traits for rs2908292.
    Found 2 traits for rs997658060.
    Found 2 traits for rs995145323.
    Found 2 traits for rs994051438.
    Found 2 traits for rs993749175.
    Found 2 traits for rs991345515.
    Found 2 traits for rs990322706.
    Found 2 traits for rs987270886.
    Found 2 traits for rs976572617.
    Found 2 traits for rs975197152.
    Found 2 traits for rs974742724.
    Found 2 traits for rs969036491.
    Found 2 traits for rs967043661.
    Found 2 traits for rs9604171.
    Found 2 traits for rs959586683.
    Found 2 traits for rs959227807.
    Found 2 traits for rs958560696.
    Found 2 traits for rs957545383.
    Found 2 traits for rs955981614.
    Found 2 traits for rs950675668.
    Found 2 traits for rs950049780.
    Found 2 traits for rs949943356.
    Found 2 traits for rs949204282.
    Found 2 traits for rs947078247.
    Found 2 traits for rs946244174.
    Found 2 traits for rs945379630.
    Found 2 traits for rs944659757.
    Found 2 traits for rs942934493.
    Found 2 traits for rs941572665.
    Found 2 traits for rs939252020.
    Found 2 traits for rs938331976.
    Found 2 traits for rs935637877.
    Found 2 traits for rs931906248.
    Found 2 traits for rs9272363.
    Found 2 traits for rs9272353.
    Found 2 traits for rs9271594.
    Found 2 traits for rs9268844.
    Found 3 traits for rs9268522.
    Found 2 traits for rs9268455.
    Found 2 traits for rs921890878.
    Found 2 traits for rs920463313.
    Found 2 traits for rs920377342.
    Found 2 traits for rs918393304.
    Found 2 traits for rs914982993.
    Found 2 traits for rs906834295.
    Found 2 traits for rs906741775.
    Found 2 traits for rs905950847.
    Found 2 traits for rs905835670.
    Found 2 traits for rs903887193.
    Found 2 traits for rs903391718.
    Found 2 traits for rs903337086.
    Found 2 traits for rs903253466.
    Found 2 traits for rs903117011.
    Found 2 traits for rs902725266.
    Found 2 traits for rs902214963.
    Found 2 traits for rs893629637.
    Found 2 traits for rs891954249.
    Found 2 traits for rs891280128.
    Found 2 traits for rs890196119.
    Found 2 traits for rs890005603.
    Found 2 traits for rs889994155.
    Found 2 traits for rs889487643.
    Found 2 traits for rs879878849.
    Found 2 traits for rs879792800.
    Found 2 traits for rs879739678.
    Found 2 traits for rs879685982.
    Found 2 traits for rs879031925.
    Found 2 traits for rs879025335.
    Found 2 traits for rs868538362.
    Found 2 traits for rs867108694.
    Found 2 traits for rs866049921.
    Found 2 traits for rs8066507.
    Found 2 traits for rs79920422.
    Found 2 traits for rs79804154.
    Found 2 traits for rs79769504.
    Found 2 traits for rs796329474.
    Found 2 traits for rs78979751.
    Found 2 traits for rs78455862.
    Found 2 traits for rs78128943.
    Found 2 traits for rs780466682.
    Found 2 traits for rs77961302.
    Found 2 traits for rs7770010.
    Found 2 traits for rs7766978.
    Found 2 traits for rs774698563.
    Found 2 traits for rs773334844.
    Found 2 traits for rs771656340.
    Found 2 traits for rs771041661.
    Found 2 traits for rs770911369.
    Found 2 traits for rs76918399.
    Found 2 traits for rs765534905.
    Found 2 traits for rs763087992.
    Found 2 traits for rs762345986.
    Found 2 traits for rs76205446.
    Found 2 traits for rs761833241.
    Found 2 traits for rs761486125.
    Found 2 traits for rs759633872.
    Found 2 traits for rs759515809.
    Found 2 traits for rs759417989.
    Found 2 traits for rs759082743.
    Found 2 traits for rs75858218.
    Found 2 traits for rs758518862.
    Found 2 traits for rs753649749.
    Found 2 traits for rs746693788.
    Found 2 traits for rs74589403.
    Found 2 traits for rs7449585.
    Found 2 traits for rs73465766.
    Found 2 traits for rs7343130.
    Found 2 traits for rs7290470.
    Found 2 traits for rs7249607.
    Found 2 traits for rs71260329.
    Found 2 traits for rs71190749.
    Found 2 traits for rs71171294.
    Found 2 traits for rs66586168.
    Found 2 traits for rs664477.
    Found 2 traits for rs6511738.
    Found 2 traits for rs62393574.
    Found 2 traits for rs62288696.
    Found 2 traits for rs62141985.
    Found 2 traits for rs62117204.
    Found 2 traits for rs62117161.
    Found 2 traits for rs62107009.
    Found 2 traits for rs617578.
    Found 2 traits for rs61642202.
    Found 2 traits for rs61422252.
    Found 2 traits for rs61012123.
    Found 2 traits for rs60256690.
    Found 2 traits for rs601148.
    Found 2 traits for rs601020.
    Found 2 traits for rs59678362.
    Found 2 traits for rs58810411.
    Found 2 traits for rs5875381.
    Found 2 traits for rs58455045.
    Found 2 traits for rs5813819.
    Found 2 traits for rs57907894.
    Found 2 traits for rs57742507.
    Found 2 traits for rs577330509.
    Found 2 traits for rs577038368.
    Found 2 traits for rs576468917.
    Found 2 traits for rs575933971.
    Found 2 traits for rs574995791.
    Found 2 traits for rs574655882.
    Found 2 traits for rs573469061.
    Found 2 traits for rs57337864.
    Found 2 traits for rs572893139.
    Found 2 traits for rs570743232.
    Found 2 traits for rs56994395.
    Found 2 traits for rs569925552.
    Found 2 traits for rs569705402.
    Found 2 traits for rs569481169.
    Found 2 traits for rs568528046.
    Found 2 traits for rs568319760.
    Found 2 traits for rs566358991.
    Found 2 traits for rs565334527.
    Found 2 traits for rs565046070.
    Found 2 traits for rs564101029.
    Found 2 traits for rs56369833.
    Found 2 traits for rs56283909.
    Found 2 traits for rs562305514.
    Found 2 traits for rs562289.
    Found 2 traits for rs561741258.
    Found 2 traits for rs560268575.
    Found 2 traits for rs559986131.
    Found 2 traits for rs558450407.
    Found 2 traits for rs557990794.
    Found 2 traits for rs557051451.
    Found 2 traits for rs556209078.
    Found 2 traits for rs553736939.
    Found 2 traits for rs552223287.
    Found 2 traits for rs548960608.
    Found 2 traits for rs548532112.
    Found 2 traits for rs548469999.
    Found 2 traits for rs547260551.
    Found 2 traits for rs546019839.
    Found 2 traits for rs545361786.
    Found 2 traits for rs544301146.
    Found 2 traits for rs541977342.
    Found 2 traits for rs538568659.
    Found 2 traits for rs537418221.
    Found 2 traits for rs537298290.
    Found 2 traits for rs536145993.
    Found 2 traits for rs536039270.
    Found 2 traits for rs535943537.
    Found 2 traits for rs535744882.
    Found 2 traits for rs534052504.
    Found 2 traits for rs533822042.
    Found 2 traits for rs532415821.
    Found 2 traits for rs531438766.
    Found 2 traits for rs530555010.
    Found 2 traits for rs528671660.
    Found 2 traits for rs527605160.
    Found 2 traits for rs519825.
    Found 2 traits for rs508318.
    Found 2 traits for rs4802241.
    Found 2 traits for rs4547394.
    Found 2 traits for rs4394648.
    Found 2 traits for rs4347564.
    Found 2 traits for rs41289514.
    Found 2 traits for rs3872719.
    Found 2 traits for rs3793127.
    Found 2 traits for rs377744233.
    Found 2 traits for rs376690477.
    Found 2 traits for rs374903395.
    Found 2 traits for rs374049962.
    Found 2 traits for rs373822597.
    Found 2 traits for rs372237806.
    Found 2 traits for rs370350367.
    Found 2 traits for rs369041631.
    Found 2 traits for rs368848940.
    Found 2 traits for rs368776529.
    Found 2 traits for rs368342230.
    Found 2 traits for rs368340812.
    Found 2 traits for rs368042471.
    Found 2 traits for rs367921783.
    Found 2 traits for rs35733971.
    Found 2 traits for rs35647923.
    Found 2 traits for rs34798982.
    Found 2 traits for rs34606745.
    Found 2 traits for rs34562807.
    Found 2 traits for rs34165484.
    Found 2 traits for rs34111552.
    Found 2 traits for rs3129753.
    Found 2 traits for rs3129751.
    Found 2 traits for rs3129748.
    Found 2 traits for rs3129747.
    Found 2 traits for rs3104381.
    Found 2 traits for rs3104378.
    Found 2 traits for rs3104368.
    Found 2 traits for rs2996647.
    Found 2 traits for rs28463544.
    Found 2 traits for rs28368484.
    Found 2 traits for rs2760990.
    Found 2 traits for rs2758559.
    Found 2 traits for rs2546403.
    Found 2 traits for rs2150920593.
    Found 2 traits for rs2124551100.
    Found 2 traits for rs2120001636.
    Found 2 traits for rs2093468087.
    Found 2 traits for rs2085530621.
    Found 2 traits for rs2083750537.
    Found 2 traits for rs2079977919.
    Found 2 traits for rs2076432782.
    Found 2 traits for rs2075977616.
    Found 2 traits for rs2073397987.
    Found 2 traits for rs2065957087.
    Found 2 traits for rs2064056109.
    Found 2 traits for rs2064056060.
    Found 2 traits for rs2053750497.
    Found 2 traits for rs2052996149.
    Found 2 traits for rs2052164865.
    Found 2 traits for rs2051320721.
    Found 2 traits for rs2042404240.
    Found 2 traits for rs2041518089.
    Found 2 traits for rs2040147005.
    Found 2 traits for rs2040076095.
    Found 2 traits for rs2038153903.
    Found 2 traits for rs2037732492.
    Found 2 traits for rs2037357773.
    Found 2 traits for rs2035619952.
    Found 2 traits for rs2035619840.
    Found 2 traits for rs2035619712.
    Found 2 traits for rs2030065142.
    Found 2 traits for rs202108135.
    Found 2 traits for rs202037943.
    Found 2 traits for rs201208642.
    Found 2 traits for rs201189859.
    Found 2 traits for rs200726585.
    Found 2 traits for rs200722375.
    Found 2 traits for rs200628965.
    Found 2 traits for rs199945687.
    Found 2 traits for rs199638393.
    Found 2 traits for rs199595584.
    Found 2 traits for rs199551441.
    Found 2 traits for rs1985691.
    Found 2 traits for rs1983727065.
    Found 2 traits for rs1970262824.
    Found 2 traits for rs1951611943.
    Found 2 traits for rs193249943.
    Found 2 traits for rs1909747014.
    Found 2 traits for rs1906375469.
    Found 2 traits for rs1905486762.
    Found 2 traits for rs1904751435.
    Found 2 traits for rs190224580.
    Found 2 traits for rs190180107.
    Found 2 traits for rs1880782601.
    Found 2 traits for rs1875936746.
    Found 2 traits for rs186218196.
    Found 2 traits for rs1855026528.
    Found 2 traits for rs1840010844.
    Found 2 traits for rs1836903150.
    Found 2 traits for rs1836070657.
    Found 2 traits for rs1833286573.
    Found 2 traits for rs183251182.
    Found 2 traits for rs1822346958.
    Found 2 traits for rs1822289798.
    Found 2 traits for rs1822106862.
    Found 2 traits for rs1818800972.
    Found 2 traits for rs1802665945.
    Found 2 traits for rs1789406952.
    Found 2 traits for rs1784729937.
    Found 2 traits for rs1778672500.
    Found 2 traits for rs1776228526.
    Found 2 traits for rs1744306563.
    Found 2 traits for rs1743330765.
    Found 2 traits for rs1740607393.
    Found 2 traits for rs1739878299.
    Found 2 traits for rs1723794096.
    Found 2 traits for rs1717431194.
    Found 2 traits for rs1713432349.
    Found 2 traits for rs1707023343.
    Found 2 traits for rs1690388098.
    Found 2 traits for rs1681024656.
    Found 2 traits for rs1681023547.
    Found 2 traits for rs1679763294.
    Found 2 traits for rs1669197221.
    Found 2 traits for rs1661880714.
    Found 2 traits for rs1661417525.
    Found 2 traits for rs1658217273.
    Found 2 traits for rs1658150431.
    Found 2 traits for rs1657042371.
    Found 2 traits for rs1652576846.
    Found 2 traits for rs1648038996.
    Found 2 traits for rs1646657755.
    Found 2 traits for rs1646657652.
    Found 2 traits for rs1644420296.
    Found 2 traits for rs1643690279.
    Found 2 traits for rs1642424605.
    Found 2 traits for rs1597705854.
    Found 2 traits for rs1596645121.
    Found 2 traits for rs1592939473.
    Found 2 traits for rs1590909847.
    Found 2 traits for rs1590420364.
    Found 2 traits for rs1587397386.
    Found 2 traits for rs1585262241.
    Found 2 traits for rs1575851053.
    Found 2 traits for rs1568254901.
    Found 2 traits for rs1567352683.
    Found 2 traits for rs1566582849.
    Found 2 traits for rs1563078617.
    Found 2 traits for rs1563072994.
    Found 2 traits for rs1562494598.
    Found 2 traits for rs1560455525.
    Found 2 traits for rs1555998308.
    Found 2 traits for rs1555964346.
    Found 2 traits for rs1555821715.
    Found 2 traits for rs1555789087.
    Found 2 traits for rs1555709908.
    Found 2 traits for rs1555620989.
    Found 2 traits for rs1555609987.
    Found 2 traits for rs1555476996.
    Found 2 traits for rs1555465179.
    Found 2 traits for rs1555331243.
    Found 2 traits for rs1555321571.
    Found 2 traits for rs1555154730.
    Found 2 traits for rs1555074577.
    Found 2 traits for rs1555074574.
    Found 2 traits for rs1555073412.
    Found 2 traits for rs1555051013.
    Found 2 traits for rs1555050988.
    Found 2 traits for rs1554726695.
    Found 2 traits for rs1554601497.
    Found 2 traits for rs1554601495.
    Found 2 traits for rs1554573754.
    Found 2 traits for rs1554532051.
    Found 2 traits for rs1554531072.
    Found 2 traits for rs1554531069.
    Found 2 traits for rs1554337191.
    Found 2 traits for rs1553553653.
    Found 2 traits for rs1553361219.
    Found 2 traits for rs1553202098.
    Found 2 traits for rs1553173615.
    Found 2 traits for rs150685845.
    Found 2 traits for rs150326641.
    Found 2 traits for rs150291783.
    Found 2 traits for rs1491387339.
    Found 2 traits for rs1491369607.
    Found 2 traits for rs1491105501.
    Found 2 traits for rs1489928357.
    Found 2 traits for rs1488544082.
    Found 2 traits for rs1487030640.
    Found 2 traits for rs1485554844.
    Found 2 traits for rs1485429079.
    Found 2 traits for rs1485351806.
    Found 2 traits for rs1484578739.
    Found 2 traits for rs1484419955.
    Found 2 traits for rs1484030501.
    Found 2 traits for rs1483773823.
    Found 2 traits for rs1483765759.
    Found 2 traits for rs1483504988.
    Found 2 traits for rs1483340178.
    Found 2 traits for rs1482211676.
    Found 2 traits for rs1482168704.
    Found 2 traits for rs1481218172.
    Found 2 traits for rs1480643073.
    Found 2 traits for rs1479717350.
    Found 2 traits for rs1478230788.
    Found 2 traits for rs1477539403.
    Found 2 traits for rs1477026225.
    Found 2 traits for rs1476682091.
    Found 2 traits for rs1476023478.
    Found 2 traits for rs1475747079.
    Found 2 traits for rs1475548985.
    Found 2 traits for rs1475471251.
    Found 2 traits for rs1474832594.
    Found 2 traits for rs1474019140.
    Found 2 traits for rs1473940278.
    Found 2 traits for rs1473834293.
    Found 2 traits for rs1472697447.
    Found 2 traits for rs147252057.
    Found 2 traits for rs1472108032.
    Found 2 traits for rs1472081286.
    Found 2 traits for rs1471550107.
    Found 2 traits for rs1470992668.
    Found 2 traits for rs1470817156.
    Found 2 traits for rs1470310616.
    Found 2 traits for rs1470069776.
    Found 2 traits for rs1469740920.
    Found 2 traits for rs1469424262.
    Found 2 traits for rs1469263388.
    Found 2 traits for rs146896044.
    Found 2 traits for rs1467612191.
    Found 2 traits for rs1465759459.
    Found 2 traits for rs1465685846.
    Found 2 traits for rs1465355970.
    Found 2 traits for rs1464017073.
    Found 2 traits for rs1462977024.
    Found 2 traits for rs146192223.
    Found 2 traits for rs1461907371.
    Found 2 traits for rs1461827110.
    Found 2 traits for rs1461072994.
    Found 2 traits for rs1459112573.
    Found 2 traits for rs1458520743.
    Found 2 traits for rs1458056517.
    Found 2 traits for rs1457264965.
    Found 2 traits for rs1456752008.
    Found 2 traits for rs1456374074.
    Found 2 traits for rs1455936159.
    Found 2 traits for rs1455283239.
    Found 2 traits for rs1454695433.
    Found 2 traits for rs1452849601.
    Found 2 traits for rs1452815376.
    Found 2 traits for rs1450411286.
    Found 2 traits for rs144767421.
    Found 2 traits for rs1445715340.
    Found 2 traits for rs1445575576.
    Found 2 traits for rs1445466240.
    Found 2 traits for rs1445112456.
    Found 2 traits for rs1444368239.
    Found 2 traits for rs1443840515.
    Found 2 traits for rs1443689893.
    Found 2 traits for rs144328302.
    Found 2 traits for rs1442475248.
    Found 2 traits for rs1442217664.
    Found 2 traits for rs1441804178.
    Found 2 traits for rs1441508801.
    Found 2 traits for rs1439737489.
    Found 2 traits for rs1439643019.
    Found 2 traits for rs143962334.
    Found 2 traits for rs143943467.
    Found 2 traits for rs1438440014.
    Found 2 traits for rs1435170216.
    Found 2 traits for rs1432629908.
    Found 2 traits for rs143180426.
    Found 2 traits for rs1431252608.
    Found 2 traits for rs1430663358.
    Found 2 traits for rs143019611.
    Found 2 traits for rs1429724769.
    Found 2 traits for rs1428838377.
    Found 2 traits for rs1428666075.
    Found 2 traits for rs1427302619.
    Found 2 traits for rs1425955889.
    Found 2 traits for rs1424900334.
    Found 2 traits for rs1423973664.
    Found 2 traits for rs1423662667.
    Found 2 traits for rs142283076.
    Found 2 traits for rs142229517.
    Found 2 traits for rs142188527.
    Found 2 traits for rs1421858724.
    Found 2 traits for rs1421604029.
    Found 2 traits for rs1421155854.
    Found 2 traits for rs1417363683.
    Found 2 traits for rs1416466766.
    Found 2 traits for rs1416175390.
    Found 2 traits for rs1415730747.
    Found 2 traits for rs1414985603.
    Found 2 traits for rs1414168601.
    Found 2 traits for rs1413522490.
    Found 2 traits for rs1413323051.
    Found 2 traits for rs1412811394.
    Found 2 traits for rs1411904646.
    Found 2 traits for rs1411623251.
    Found 2 traits for rs1409928359.
    Found 2 traits for rs1409228577.
    Found 2 traits for rs140886023.
    Found 2 traits for rs1408747551.
    Found 2 traits for rs1408180684.
    Found 2 traits for rs1408113834.
    Found 2 traits for rs1407756628.
    Found 2 traits for rs1407236131.
    Found 2 traits for rs1407033483.
    Found 2 traits for rs1404913201.
    Found 2 traits for rs1404822667.
    Found 2 traits for rs1404224919.
    Found 2 traits for rs1404067778.
    Found 2 traits for rs1404018243.
    Found 2 traits for rs140390502.
    Found 2 traits for rs1403284517.
    Found 2 traits for rs1403003350.
    Found 2 traits for rs1402152841.
    Found 2 traits for rs1401992092.
    Found 2 traits for rs1401920475.
    Found 2 traits for rs1400393603.
    Found 2 traits for rs1398444737.
    Found 2 traits for rs1397842965.
    Found 2 traits for rs1397586721.
    Found 2 traits for rs1396957395.
    Found 2 traits for rs1396035227.
    Found 2 traits for rs1395280731.
    Found 2 traits for rs1395183799.
    Found 2 traits for rs1395049388.
    Found 2 traits for rs1394896178.
    Found 2 traits for rs1394091179.
    Found 2 traits for rs1393967520.
    Found 2 traits for rs139326841.
    Found 2 traits for rs1392734500.
    Found 2 traits for rs1391812258.
    Found 2 traits for rs1391504551.
    Found 2 traits for rs1390937290.
    Found 2 traits for rs1389719881.
    Found 2 traits for rs1389132849.
    Found 2 traits for rs1388898746.
    Found 2 traits for rs1388705722.
    Found 2 traits for rs1388282779.
    Found 2 traits for rs1387462349.
    Found 2 traits for rs1386709764.
    Found 2 traits for rs1385983626.
    Found 2 traits for rs1385628861.
    Found 2 traits for rs1385582538.
    Found 2 traits for rs1385472106.
    Found 2 traits for rs1385078687.
    Found 2 traits for rs1382795820.
    Found 2 traits for rs1382775283.
    Found 2 traits for rs138238485.
    Found 2 traits for rs1381828529.
    Found 2 traits for rs1379529633.
    Found 2 traits for rs1378802782.
    Found 2 traits for rs1378323638.
    Found 2 traits for rs1377788735.
    Found 2 traits for rs1376535353.
    Found 2 traits for rs1376370722.
    Found 2 traits for rs1376194951.
    Found 2 traits for rs1375871492.
    Found 2 traits for rs1375566941.
    Found 2 traits for rs1374926972.
    Found 2 traits for rs1374854652.
    Found 2 traits for rs1374443291.
    Found 2 traits for rs1372913625.
    Found 2 traits for rs1372844864.
    Found 2 traits for rs1372704444.
    Found 2 traits for rs1371643882.
    Found 2 traits for rs1371401738.
    Found 2 traits for rs1371331260.
    Found 2 traits for rs1370965392.
    Found 2 traits for rs1370017161.
    Found 2 traits for rs1369563818.
    Found 2 traits for rs1368723920.
    Found 2 traits for rs1368642558.
    Found 2 traits for rs1368039384.
    Found 2 traits for rs1367265225.
    Found 2 traits for rs1366382657.
    Found 2 traits for rs1364541871.
    Found 2 traits for rs1364149896.
    Found 2 traits for rs1363962018.
    Found 2 traits for rs1362397835.
    Found 2 traits for rs1360681730.
    Found 2 traits for rs1359118268.
    Found 2 traits for rs1359100579.
    Found 2 traits for rs1358250852.
    Found 2 traits for rs1358228775.
    Found 2 traits for rs1357694804.
    Found 2 traits for rs1356120401.
    Found 2 traits for rs1355670372.
    Found 2 traits for rs1355274306.
    Found 2 traits for rs1355114191.
    Found 2 traits for rs1354347537.
    Found 2 traits for rs1353668650.
    Found 2 traits for rs1352922508.
    Found 2 traits for rs1352071596.
    Found 2 traits for rs1351881089.
    Found 2 traits for rs1351651013.
    Found 2 traits for rs1351359673.
    Found 2 traits for rs1350645584.
    Found 2 traits for rs1350229578.
    Found 2 traits for rs1349978361.
    Found 2 traits for rs1349859144.
    Found 2 traits for rs1349409080.
    Found 2 traits for rs1349200192.
    Found 2 traits for rs1349126896.
    Found 2 traits for rs1348229080.
    Found 2 traits for rs1347602558.
    Found 2 traits for rs1347265103.
    Found 2 traits for rs1347052251.
    Found 2 traits for rs1345200633.
    Found 2 traits for rs1344764494.
    Found 2 traits for rs1343350294.
    Found 2 traits for rs1342643975.
    Found 2 traits for rs1341660992.
    Found 2 traits for rs1340910297.
    Found 2 traits for rs1340901888.
    Found 2 traits for rs1340254507.
    Found 2 traits for rs1340008592.
    Found 2 traits for rs1339933971.
    Found 2 traits for rs1339749404.
    Found 2 traits for rs1339702970.
    Found 2 traits for rs1338208268.
    Found 2 traits for rs1336810492.
    Found 2 traits for rs1335344166.
    Found 2 traits for rs1334993567.
    Found 2 traits for rs1334630696.
    Found 2 traits for rs13337595.
    Found 2 traits for rs1333742182.
    Found 2 traits for rs1332932956.
    Found 2 traits for rs1332897454.
    Found 2 traits for rs1332151591.
    Found 2 traits for rs1331553624.
    Found 2 traits for rs1331439083.
    Found 2 traits for rs1331156689.
    Found 2 traits for rs1330271270.
    Found 2 traits for rs1329444417.
    Found 2 traits for rs1328984647.
    Found 2 traits for rs1327088179.
    Found 2 traits for rs1325649112.
    Found 2 traits for rs1325254595.
    Found 2 traits for rs1324865687.
    Found 2 traits for rs1323625018.
    Found 2 traits for rs1323565438.
    Found 2 traits for rs1321925127.
    Found 2 traits for rs1321375918.
    Found 2 traits for rs1318606417.
    Found 2 traits for rs1318500235.
    Found 2 traits for rs1318048165.
    Found 2 traits for rs1317768370.
    Found 2 traits for rs1316966968.
    Found 2 traits for rs1316952114.
    Found 2 traits for rs13159363.
    Found 2 traits for rs1315875949.
    Found 2 traits for rs1314452460.
    Found 2 traits for rs1314416129.
    Found 2 traits for rs1312893620.
    Found 2 traits for rs1312458002.
    Found 2 traits for rs1311911699.
    Found 2 traits for rs1311463499.
    Found 2 traits for rs1311427302.
    Found 2 traits for rs1311117768.
    Found 2 traits for rs1310494836.
    Found 2 traits for rs1308706830.
    Found 2 traits for rs1308349799.
    Found 2 traits for rs1307496930.
    Found 2 traits for rs1306953234.
    Found 2 traits for rs1306818289.
    Found 2 traits for rs1306536849.
    Found 2 traits for rs1306405136.
    Found 2 traits for rs1306363346.
    Found 2 traits for rs1305401557.
    Found 2 traits for rs1305260347.
    Found 2 traits for rs1304462752.
    Found 2 traits for rs1304344790.
    Found 2 traits for rs1304078336.
    Found 2 traits for rs1303095550.
    Found 2 traits for rs1303093395.
    Found 2 traits for rs1302697184.
    Found 2 traits for rs1300768323.
    Found 2 traits for rs1299853547.
    Found 2 traits for rs1299512285.
    Found 2 traits for rs1298939524.
    Found 2 traits for rs1298828254.
    Found 2 traits for rs1298189613.
    Found 2 traits for rs1297364356.
    Found 2 traits for rs1297156851.
    Found 2 traits for rs1296731650.
    Found 2 traits for rs1296656080.
    Found 2 traits for rs1296008867.
    Found 2 traits for rs1295714207.
    Found 2 traits for rs1295517516.
    Found 2 traits for rs1295446575.
    Found 2 traits for rs1295331050.
    Found 2 traits for rs1294325936.
    Found 2 traits for rs1293661154.
    Found 2 traits for rs1293514950.
    Found 2 traits for rs1292982927.
    Found 2 traits for rs1292259832.
    Found 2 traits for rs1291738594.
    Found 2 traits for rs1291362090.
    Found 2 traits for rs1290948092.
    Found 2 traits for rs1290866150.
    Found 2 traits for rs1290134825.
    Found 2 traits for rs1288116823.
    Found 2 traits for rs1287529016.
    Found 2 traits for rs1286972096.
    Found 2 traits for rs1284394562.
    Found 2 traits for rs1283930218.
    Found 2 traits for rs1283541332.
    Found 2 traits for rs1283490962.
    Found 2 traits for rs1282750627.
    Found 2 traits for rs1282630843.
    Found 2 traits for rs1282434985.
    Found 2 traits for rs1281842209.
    Found 2 traits for rs1281813435.
    Found 2 traits for rs1281628355.
    Found 2 traits for rs1281146788.
    Found 2 traits for rs1278738107.
    Found 2 traits for rs1277391500.
    Found 2 traits for rs1276903792.
    Found 2 traits for rs1275756507.
    Found 2 traits for rs1274427570.
    Found 2 traits for rs1274020366.
    Found 2 traits for rs12736080.
    Found 2 traits for rs1273153391.
    Found 2 traits for rs1273071898.
    Found 2 traits for rs1272598863.
    Found 2 traits for rs1271765809.
    Found 2 traits for rs1270383603.
    Found 2 traits for rs1269322417.
    Found 2 traits for rs1269074462.
    Found 2 traits for rs1269046383.
    Found 2 traits for rs1268187921.
    Found 2 traits for rs1267581371.
    Found 2 traits for rs1267492347.
    Found 2 traits for rs1267131333.
    Found 2 traits for rs1265889286.
    Found 2 traits for rs1265782083.
    Found 2 traits for rs1265426063.
    Found 2 traits for rs1265024412.
    Found 2 traits for rs1264912848.
    Found 2 traits for rs1264751901.
    Found 2 traits for rs1264652607.
    Found 2 traits for rs1264236841.
    Found 2 traits for rs1264221554.
    Found 2 traits for rs1264199617.
    Found 2 traits for rs1262184171.
    Found 2 traits for rs12614792.
    Found 2 traits for rs1261343213.
    Found 2 traits for rs1261220185.
    Found 2 traits for rs1259975228.
    Found 2 traits for rs1259238984.
    Found 2 traits for rs1257925295.
    Found 2 traits for rs1257773292.
    Found 2 traits for rs1257128018.
    Found 2 traits for rs1255811814.
    Found 2 traits for rs1255709951.
    Found 2 traits for rs1254742144.
    Found 2 traits for rs1254559012.
    Found 2 traits for rs1253147039.
    Found 2 traits for rs1252579567.
    Found 2 traits for rs1252500162.
    Found 2 traits for rs1252354207.
    Found 2 traits for rs1251919664.
    Found 2 traits for rs1250990056.
    Found 2 traits for rs1249641444.
    Found 2 traits for rs1249242045.
    Found 2 traits for rs1248734336.
    Found 2 traits for rs1247800597.
    Found 2 traits for rs1247157380.
    Found 2 traits for rs12462536.
    Found 2 traits for rs1246130952.
    Found 2 traits for rs1246009187.
    Found 2 traits for rs1245037762.
    Found 2 traits for rs1244433189.
    Found 2 traits for rs1244239771.
    Found 2 traits for rs1243800619.
    Found 2 traits for rs1243638640.
    Found 2 traits for rs1243122814.
    Found 2 traits for rs1242963535.
    Found 2 traits for rs1242199581.
    Found 2 traits for rs1241385483.
    Found 2 traits for rs1241129008.
    Found 2 traits for rs1240859920.
    Found 2 traits for rs1240189301.
    Found 2 traits for rs1240119130.
    Found 2 traits for rs1239483384.
    Found 2 traits for rs1239388321.
    Found 2 traits for rs1237948850.
    Found 2 traits for rs1237780657.
    Found 2 traits for rs1237656557.
    Found 2 traits for rs1235961115.
    Found 2 traits for rs1233962092.
    Found 2 traits for rs1233069191.
    Found 2 traits for rs1232901130.
    Found 2 traits for rs1232620381.
    Found 2 traits for rs1232324269.
    Found 2 traits for rs1231827674.
    Found 2 traits for rs1231605654.
    Found 2 traits for rs1231327791.
    Found 2 traits for rs1230863855.
    Found 2 traits for rs1229916131.
    Found 2 traits for rs1227758043.
    Found 2 traits for rs1225330562.
    Found 2 traits for rs1225279127.
    Found 2 traits for rs1224926429.
    Found 2 traits for rs1224276847.
    Found 2 traits for rs1223739912.
    Found 2 traits for rs1223720719.
    Found 2 traits for rs1223058726.
    Found 2 traits for rs1221908556.
    Found 2 traits for rs1221662457.
    Found 2 traits for rs1221147047.
    Found 2 traits for rs1220304375.
    Found 2 traits for rs1219833709.
    Found 2 traits for rs1219080044.
    Found 2 traits for rs1218851442.
    Found 2 traits for rs1218372872.
    Found 2 traits for rs1217460643.
    Found 2 traits for rs1216572284.
    Found 2 traits for rs1215624998.
    Found 2 traits for rs1214091512.
    Found 2 traits for rs1214054796.
    Found 2 traits for rs1213918414.
    Found 2 traits for rs1213636743.
    Found 2 traits for rs1213491372.
    Found 2 traits for rs1213132600.
    Found 2 traits for rs1212560546.
    Found 2 traits for rs1210224922.
    Found 2 traits for rs1210150502.
    Found 2 traits for rs1210025951.
    Found 2 traits for rs1209602347.
    Found 2 traits for rs1209568272.
    Found 2 traits for rs1208805700.
    Found 2 traits for rs1208311413.
    Found 2 traits for rs1208010228.
    Found 2 traits for rs1207599796.
    Found 2 traits for rs1206929678.
    Found 2 traits for rs1206340214.
    Found 2 traits for rs1206123205.
    Found 2 traits for rs1205865847.
    Found 2 traits for rs1205506154.
    Found 2 traits for rs1203948313.
    Found 2 traits for rs1203341863.
    Found 2 traits for rs1202324392.
    Found 2 traits for rs1201639171.
    Found 2 traits for rs1201608045.
    Found 2 traits for rs1201387997.
    Found 2 traits for rs1200379345.
    Found 2 traits for rs1200144773.
    Found 2 traits for rs1200022958.
    Found 2 traits for rs1199092550.
    Found 2 traits for rs1198553262.
    Found 2 traits for rs1198422174.
    Found 2 traits for rs1197599157.
    Found 2 traits for rs1197488771.
    Found 2 traits for rs1196563547.
    Found 2 traits for rs1196020873.
    Found 2 traits for rs1195724496.
    Found 2 traits for rs1194281258.
    Found 2 traits for rs1191974147.
    Found 2 traits for rs1191304654.
    Found 2 traits for rs1190483560.
    Found 2 traits for rs1189518681.
    Found 2 traits for rs1186350373.
    Found 2 traits for rs1185918181.
    Found 2 traits for rs1185379301.
    Found 2 traits for rs1184974643.
    Found 2 traits for rs1184157092.
    Found 2 traits for rs1183474232.
    Found 2 traits for rs1182056131.
    Found 2 traits for rs1182012625.
    Found 2 traits for rs1180886598.
    Found 2 traits for rs1179715974.
    Found 2 traits for rs1179689749.
    Found 2 traits for rs1179509817.
    Found 2 traits for rs1179471099.
    Found 2 traits for rs1179018119.
    Found 2 traits for rs1178610444.
    Found 2 traits for rs1177909976.
    Found 2 traits for rs1177181245.
    Found 2 traits for rs1175681088.
    Found 2 traits for rs1174607704.
    Found 2 traits for rs1174371704.
    Found 2 traits for rs1173935228.
    Found 2 traits for rs1172964571.
    Found 2 traits for rs1172668780.
    Found 2 traits for rs1172600966.
    Found 2 traits for rs1172108720.
    Found 2 traits for rs1170846268.
    Found 2 traits for rs1170313679.
    Found 2 traits for rs1169595633.
    Found 2 traits for rs1168992261.
    Found 2 traits for rs1168684570.
    Found 2 traits for rs1168533702.
    Found 2 traits for rs1168284838.
    Found 2 traits for rs1168094839.
    Found 2 traits for rs1167605935.
    Found 2 traits for rs1165639521.
    Found 2 traits for rs1164552231.
    Found 2 traits for rs1163770121.
    Found 2 traits for rs1163397286.
    Found 2 traits for rs1162255140.
    Found 2 traits for rs1162057875.
    Found 2 traits for rs1160098113.
    Found 2 traits for rs1159657792.
    Found 2 traits for rs1157699025.
    Found 2 traits for rs1157245202.
    Found 2 traits for rs1156259878.
    Found 2 traits for rs114236584.
    Found 2 traits for rs113771521.
    Found 2 traits for rs113740336.
    Found 2 traits for rs113717364.
    Found 2 traits for rs113532033.
    Found 2 traits for rs113522288.
    Found 2 traits for rs11333392.
    Found 2 traits for rs11291088.
    Found 2 traits for rs112405270.
    Found 2 traits for rs112260099.
    Found 2 traits for rs112236497.
    Found 2 traits for rs111708809.
    Found 2 traits for rs1114832.
    Found 2 traits for rs111407307.
    Found 2 traits for rs11121495.
    Found 2 traits for rs1057509217.
    Found 2 traits for rs1053968571.
    Found 2 traits for rs1052317533.
    Found 2 traits for rs1046138470.
    Found 2 traits for rs1045685494.
    Found 2 traits for rs1044208812.
    Found 2 traits for rs1042663288.
    Found 2 traits for rs1041743937.
    Found 2 traits for rs10415850.
    Found 2 traits for rs1039962800.
    Found 2 traits for rs1039886548.
    Found 2 traits for rs1035837513.
    Found 2 traits for rs1034010324.
    Found 2 traits for rs1033826869.
    Found 2 traits for rs1031529869.
    Found 2 traits for rs1031320110.
    Found 2 traits for rs10265276.
    Found 2 traits for rs1023546743.
    Found 2 traits for rs10221139.
    Found 2 traits for rs1019641964.
    Found 2 traits for rs1017075640.
    Found 2 traits for rs10164986.
    Found 2 traits for rs1015482461.
    Found 2 traits for rs1013953075.
    Found 2 traits for rs1012535274.
    Found 2 traits for rs1010617612.
    Found 2 traits for rs1010545857.
    Found 2 traits for rs10097868.
    Found 2 traits for rs1008588893.
    Found 2 traits for rs1007847836.
    Found 2 traits for rs1001830266.
    Found 2 traits for chr9:92785200.
    Found 2 traits for chr9:67632973.
    Found 2 traits for chr9:6698604.
    Found 2 traits for chr9:62838432.
    Found 2 traits for chr9:42847389.
    Found 2 traits for chr9:41727997.
    Found 2 traits for chr9:40329787.
    Found 2 traits for chr9:39941434.
    Found 2 traits for chr9:34034655.
    Found 2 traits for chr9:31320722.
    Found 2 traits for chr9:31320715.
    Found 2 traits for chr9:26890151.
    Found 2 traits for chr9:2528493.
    Found 2 traits for chr9:135673019.
    Found 2 traits for chr9:129980341.
    Found 2 traits for chr9:129727696.
    Found 2 traits for chr9:129033772.
    Found 2 traits for chr9:128665286.
    Found 2 traits for chr9:123706553.
    Found 2 traits for chr9:118668202.
    Found 2 traits for chr9:110014597.
    Found 2 traits for chr9:104150379.
    Found 2 traits for chr9:104150373.
    Found 2 traits for chr8:99128207.
    Found 2 traits for chr8:9493603.
    Found 2 traits for chr8:94591747.
    Found 2 traits for chr8:80570527.
    Found 2 traits for chr8:7412675.
    Found 2 traits for chr8:60523306.
    Found 2 traits for chr8:55827470.
    Found 2 traits for chr8:47081758.
    Found 2 traits for chr8:38390825.
    Found 2 traits for chr8:38194649.
    Found 2 traits for chr8:33494177.
    Found 2 traits for chr8:33494173.
    Found 2 traits for chr8:30679870.
    Found 2 traits for chr8:144301660.
    Found 2 traits for chr8:143960466.
    Found 2 traits for chr8:13268436.
    Found 2 traits for chr8:104680838.
    Found 2 traits for chr8:102887720.
    Found 2 traits for chr8:101230534.
    Found 2 traits for chr7:89881280.
    Found 2 traits for chr7:89881278.
    Found 2 traits for chr7:75853377.
    Found 2 traits for chr7:75801752.
    Found 2 traits for chr7:75607691.
    Found 2 traits for chr7:75607689.
    Found 2 traits for chr7:74792891.
    Found 2 traits for chr7:74303748.
    Found 2 traits for chr7:74071734.
    Found 2 traits for chr7:73415961.
    Found 2 traits for chr7:73239535.
    Found 2 traits for chr7:73239519.
    Found 2 traits for chr7:72703259.
    Found 2 traits for chr7:71010361.
    Found 2 traits for chr7:69807601.
    Found 2 traits for chr7:69807598.
    Found 2 traits for chr7:68108631.
    Found 2 traits for chr7:67109054.
    Found 2 traits for chr7:65711737.
    Found 2 traits for chr7:65095078.
    Found 2 traits for chr7:5856871.
    Found 2 traits for chr7:58060792.
    Found 2 traits for chr7:58060068.
    Found 2 traits for chr7:58060060.
    Found 2 traits for chr7:51057333.
    Found 2 traits for chr7:47925128.
    Found 2 traits for chr7:47554393.
    Found 2 traits for chr7:4276876.
    Found 2 traits for chr7:32888710.
    Found 2 traits for chr7:26252136.
    Found 2 traits for chr7:1765054.
    Found 2 traits for chr7:158319527.
    Found 2 traits for chr7:155134673.
    Found 2 traits for chr7:149095930.
    Found 2 traits for chr7:148989806.
    Found 2 traits for chr7:148956743.
    Found 2 traits for chr7:140961641.
    Found 2 traits for chr7:140619733.
    Found 2 traits for chr7:138674703.
    Found 2 traits for chr7:128552494.
    Found 2 traits for chr7:102920935.
    Found 2 traits for chr7:102026581.
    Found 2 traits for chr7:101284194.
    Found 2 traits for chr7:101284191.
    Found 2 traits for chr6:88001571.
    Found 2 traits for chr6:77499064.
    Found 2 traits for chr6:73319458.
    Found 2 traits for chr6:6716029.
    Found 2 traits for chr6:66722870.
    Found 2 traits for chr6:62897569.
    Found 2 traits for chr6:43534819.
    Found 2 traits for chr6:40659693.
    Found 2 traits for chr6:34044161.
    Found 2 traits for chr6:30753765.
    Found 2 traits for chr6:30753762.
    Found 2 traits for chr6:30621247.
    Found 2 traits for chr6:25902999.
    Found 2 traits for chr6:158692007.
    Found 2 traits for chr6:151261625.
    Found 2 traits for chr6:14989159.
    Found 2 traits for chr6:148303578.
    Found 2 traits for chr6:143890911.
    Found 2 traits for chr6:139484265.
    Found 2 traits for chr6:139484262.
    Found 2 traits for chr6:12840142.
    Found 2 traits for chr6:12042382.
    Found 2 traits for chr6:118716962.
    Found 2 traits for chr6:10810629.
    Found 2 traits for chr6:101494698.
    Found 2 traits for chr5:93000816.
    Found 2 traits for chr5:93000815.
    Found 2 traits for chr5:82726070.
    Found 2 traits for chr5:76509558.
    Found 2 traits for chr5:71297967.
    Found 2 traits for chr5:69337801.
    Found 2 traits for chr5:65634189.
    Found 2 traits for chr5:63078868.
    Found 2 traits for chr5:55987899.
    Found 2 traits for chr5:179789142.
    Found 2 traits for chr5:179696510.
    Found 2 traits for chr5:179691169.
    Found 2 traits for chr5:179591395.
    Found 2 traits for chr5:179544212.
    Found 2 traits for chr5:178174212.
    Found 2 traits for chr5:176801068.
    Found 2 traits for chr5:176502362.
    Found 2 traits for chr5:168253265.
    Found 2 traits for chr5:163628931.
    Found 2 traits for chr5:163628927.
    Found 2 traits for chr5:160140248.
    Found 2 traits for chr5:146234864.
    Found 2 traits for chr5:146234861.
    Found 2 traits for chr5:141447364.
    Found 2 traits for chr5:139702543.
    Found 2 traits for chr5:139533528.
    Found 2 traits for chr5:139533525.
    Found 2 traits for chr5:139533522.
    Found 2 traits for chr5:139533514.
    Found 2 traits for chr5:139405218.
    Found 2 traits for chr5:139345874.
    Found 2 traits for chr5:139345867.
    Found 2 traits for chr5:119047353.
    Found 2 traits for chr4:9650027.
    Found 2 traits for chr4:9501126.
    Found 2 traits for chr4:9501107.
    Found 2 traits for chr4:8967028.
    Found 2 traits for chr4:84770219.
    Found 2 traits for chr4:70884321.
    Found 2 traits for chr4:56835539.
    Found 2 traits for chr4:49641607.
    Found 2 traits for chr4:49308277.
    Found 2 traits for chr4:42081999.
    Found 2 traits for chr4:39359815.
    Found 2 traits for chr4:38486000.
    Found 2 traits for chr4:2764348.
    Found 2 traits for chr4:26243074.
    Found 2 traits for chr4:21544845.
    Found 2 traits for chr4:172464489.
    Found 2 traits for chr4:159437376.
    Found 2 traits for chr4:153944779.
    Found 2 traits for chr4:151065713.
    Found 2 traits for chr4:128104101.
    Found 2 traits for chr4:120327591.
    Found 2 traits for chr4:113231218.
    Found 2 traits for chr4:102329468.
    Found 2 traits for chr4:102329460.
    Found 2 traits for chr3:75674095.
    Found 2 traits for chr3:75616418.
    Found 2 traits for chr3:75612176.
    Found 2 traits for chr3:49947243.
    Found 2 traits for chr3:49210704.
    Found 2 traits for chr3:48939528.
    Found 2 traits for chr3:48876877.
    Found 2 traits for chr3:47789474.
    Found 2 traits for chr3:40344008.
    Found 2 traits for chr3:196861715.
    Found 2 traits for chr3:196852149.
    Found 2 traits for chr3:196675754.
    Found 2 traits for chr3:196266254.
    Found 2 traits for chr3:170873157.
    Found 2 traits for chr3:15593389.
    Found 2 traits for chr3:15593380.
    Found 2 traits for chr3:151417487.
    Found 2 traits for chr3:138728773.
    Found 2 traits for chr3:128696485.
    Found 2 traits for chr3:128645253.
    Found 2 traits for chr3:127582687.
    Found 2 traits for chr3:127582685.
    Found 2 traits for chr3:125521495.
    Found 2 traits for chr3:124807408.
    Found 2 traits for chr3:121791872.
    Found 2 traits for chr3:119480415.
    Found 2 traits for chr3:109285911.
    Found 2 traits for chr22:50372984.
    Found 2 traits for chr22:49879291.
    Found 2 traits for chr22:49399428.
    Found 2 traits for chr22:46713286.
    Found 2 traits for chr22:44886986.
    Found 2 traits for chr22:41609575.
    Found 2 traits for chr22:41379102.
    Found 2 traits for chr22:40935856.
    Found 2 traits for chr22:40796022.
    Found 2 traits for chr22:39166662.
    Found 2 traits for chr22:39166463.
    Found 2 traits for chr22:38405984.
    Found 2 traits for chr22:38342004.
    Found 2 traits for chr22:38242246.
    Found 2 traits for chr22:37939702.
    Found 2 traits for chr22:36389311.
    Found 2 traits for chr22:35699725.
    Found 2 traits for chr22:29148578.
    Found 2 traits for chr22:28089534.
    Found 2 traits for chr22:25789433.
    Found 2 traits for chr22:25345441.
    Found 2 traits for chr22:24777872.
    Found 2 traits for chr22:24490949.
    Found 2 traits for chr22:23797852.
    Found 2 traits for chr22:23646338.
    Found 2 traits for chr22:22518612.
    Found 2 traits for chr22:21924568.
    Found 2 traits for chr22:20503118.
    Found 2 traits for chr22:20050079.
    Found 2 traits for chr22:19311896.
    Found 2 traits for chr22:12556740.
    Found 2 traits for chr21:9817035.
    Found 2 traits for chr21:9124671.
    Found 2 traits for chr21:45500131.
    Found 2 traits for chr21:39383052.
    Found 2 traits for chr21:39383050.
    Found 2 traits for chr21:31690330.
    Found 2 traits for chr21:28685091.
    Found 2 traits for chr21:23757294.
    Found 2 traits for chr21:10756490.
    Found 2 traits for chr21:10415600.
    Found 2 traits for chr21:10415599.
    Found 2 traits for chr20:59401313.
    Found 2 traits for chr20:51520826.
    Found 2 traits for chr20:47381012.
    Found 2 traits for chr20:47194165.
    Found 2 traits for chr20:45931856.
    Found 2 traits for chr20:43652787.
    Found 2 traits for chr20:43652786.
    Found 2 traits for chr20:43469358.
    Found 2 traits for chr20:38850804.
    Found 2 traits for chr20:37109452.
    Found 2 traits for chr20:36949046.
    Found 2 traits for chr20:36949037.
    Found 2 traits for chr20:36787692.
    Found 2 traits for chr20:36728254.
    Found 2 traits for chr20:35097276.
    Found 2 traits for chr20:34646555.
    Found 2 traits for chr20:34646551.
    Found 2 traits for chr20:32735144.
    Found 2 traits for chr20:31750080.
    Found 2 traits for chr20:31703178.
    Found 2 traits for chr2:89636593.
    Found 2 traits for chr2:88005607.
    Found 2 traits for chr2:86191352.
    Found 2 traits for chr2:85532753.
    Found 2 traits for chr2:79818457.
    Found 2 traits for chr2:65280182.
    Found 2 traits for chr2:61148337.
    Found 2 traits for chr2:54004201.
    Found 2 traits for chr2:53978264.
    Found 2 traits for chr2:46273971.
    Found 2 traits for chr2:39126426.
    Found 2 traits for chr2:39089972.
    Found 2 traits for chr2:27511478.
    Found 2 traits for chr2:27141043.
    Found 2 traits for chr2:231501097.
    Found 2 traits for chr2:219581037.
    Found 2 traits for chr2:219581029.
    Found 2 traits for chr2:205992420.
    Found 2 traits for chr2:205992418.
    Found 2 traits for chr2:202861728.
    Found 2 traits for chr2:202813151.
    Found 2 traits for chr2:202567965.
    Found 2 traits for chr2:202294096.
    Found 2 traits for chr2:202003854.
    Found 2 traits for chr2:183479004.
    Found 2 traits for chr2:157549175.
    Found 2 traits for chr2:157549149.
    Found 2 traits for chr2:135470542.
    Found 2 traits for chr2:130863966.
    Found 2 traits for chr2:130469139.
    Found 2 traits for chr2:128082350.
    Found 2 traits for chr2:128059121.
    Found 2 traits for chr2:118093497.
    Found 2 traits for chr2:113864927.
    Found 2 traits for chr2:111295877.
    Found 2 traits for chr2:101207215.
    Found 2 traits for chr19:8600010.
    Found 2 traits for chr19:8165439.
    Found 2 traits for chr19:7236842.
    Found 2 traits for chr19:56055154.
    Found 2 traits for chr19:55422574.
    Found 2 traits for chr19:55422571.
    Found 2 traits for chr19:55422561.
    Found 2 traits for chr19:54059216.
    Found 2 traits for chr19:53629086.
    Found 2 traits for chr19:49905891.
    Found 2 traits for chr19:49714558.
    Found 2 traits for chr19:48916454.
    Found 2 traits for chr19:4755334.
    Found 2 traits for chr19:47458598.
    Found 2 traits for chr19:46844567.
    Found 2 traits for chr19:46844565.
    Found 2 traits for chr19:45436408.
    Found 2 traits for chr19:4528057.
    Found 2 traits for chr19:45260080.
    Found 2 traits for chr19:45260078.
    Found 2 traits for chr19:45176297.
    Found 2 traits for chr19:44909967.
    Found 2 traits for chr19:44909521.
    Found 2 traits for chr19:44898730.
    Found 2 traits for chr19:44897776.
    Found 2 traits for chr19:44897227.
    Found 2 traits for chr19:44895459.
    Found 2 traits for chr19:44893642.
    Found 2 traits for chr19:44882783.
    Found 2 traits for chr19:44880774.
    Found 2 traits for chr19:44877713.
    Found 2 traits for chr19:44869757.
    Found 2 traits for chr19:44858325.
    Found 2 traits for chr19:44844403.
    Found 2 traits for chr19:44836881.
    Found 2 traits for chr19:44829763.
    Found 2 traits for chr19:44732598.
    Found 2 traits for chr19:44732593.
    Found 2 traits for chr19:44731001.
    Found 2 traits for chr19:44730062.
    Found 2 traits for chr19:44714520.
    Found 2 traits for chr19:4331323.
    Found 2 traits for chr19:4331320.
    Found 2 traits for chr19:4302852.
    Found 2 traits for chr19:41806330.
    Found 2 traits for chr19:41509971.
    Found 2 traits for chr19:41157626.
    Found 2 traits for chr19:40402196.
    Found 2 traits for chr19:38243249.
    Found 2 traits for chr19:37687722.
    Found 2 traits for chr19:36868792.
    Found 2 traits for chr19:36868169.
    Found 2 traits for chr19:36516403.
    Found 2 traits for chr19:35911172.
    Found 2 traits for chr19:34465420.
    Found 2 traits for chr19:34371991.
    Found 2 traits for chr19:33837044.
    Found 2 traits for chr19:33033650.
    Found 2 traits for chr19:2870231.
    Found 2 traits for chr19:2870227.
    Found 2 traits for chr19:23676856.
    Found 2 traits for chr19:23676840.
    Found 2 traits for chr19:22712189.
    Found 2 traits for chr19:22712186.
    Found 2 traits for chr19:20076324.
    Found 2 traits for chr19:20023807.
    Found 2 traits for chr19:19363796.
    Found 2 traits for chr19:1899530.
    Found 2 traits for chr19:18528048.
    Found 2 traits for chr19:17354058.
    Found 2 traits for chr19:15822927.
    Found 2 traits for chr19:15822925.
    Found 2 traits for chr19:14707697.
    Found 2 traits for chr19:14258228.
    Found 2 traits for chr19:13893922.
    Found 2 traits for chr19:13893919.
    Found 2 traits for chr19:13745457.
    Found 2 traits for chr19:12286479.
    Found 2 traits for chr19:12129144.
    Found 2 traits for chr19:11648684.
    Found 2 traits for chr18:9777902.
    Found 2 traits for chr18:9037196.
    Found 2 traits for chr18:78591455.
    Found 2 traits for chr18:76817728.
    Found 2 traits for chr18:7274664.
    Found 2 traits for chr18:7107368.
    Found 2 traits for chr18:62195985.
    Found 2 traits for chr18:62195984.
    Found 2 traits for chr18:58129746.
    Found 2 traits for chr18:54537923.
    Found 2 traits for chr18:54207819.
    Found 2 traits for chr18:54138189.
    Found 2 traits for chr18:3550114.
    Found 2 traits for chr18:23093208.
    Found 2 traits for chr18:23093206.
    Found 2 traits for chr18:12866210.
    Found 2 traits for chr17:971686.
    Found 2 traits for chr17:9113990.
    Found 2 traits for chr17:839242.
    Found 2 traits for chr17:82725483.
    Found 2 traits for chr17:81849383.
    Found 2 traits for chr17:783556.
    Found 2 traits for chr17:76098643.
    Found 2 traits for chr17:75993927.
    Found 2 traits for chr17:75993291.
    Found 2 traits for chr17:75801051.
    Found 2 traits for chr17:75801049.
    Found 2 traits for chr17:75711868.
    Found 2 traits for chr17:75529672.
    Found 2 traits for chr17:74976489.
    Found 2 traits for chr17:7055430.
    Found 2 traits for chr17:69514705.
    Found 2 traits for chr17:67064485.
    Found 2 traits for chr17:62604736.
    Found 2 traits for chr17:62388645.
    Found 2 traits for chr17:62249528.
    Found 2 traits for chr17:60703056.
    Found 2 traits for chr17:59965446.
    Found 2 traits for chr17:58478215.
    Found 2 traits for chr17:5439856.
    Found 2 traits for chr17:50126919.
    Found 2 traits for chr17:4995776.
    Found 2 traits for chr17:4993046.
    Found 2 traits for chr17:49678884.
    Found 2 traits for chr17:49143921.
    Found 2 traits for chr17:48866581.
    Found 2 traits for chr17:48098623.
    Found 2 traits for chr17:47530102.
    Found 2 traits for chr17:45079271.
    Found 2 traits for chr17:44513203.
    Found 2 traits for chr17:44372764.
    Found 2 traits for chr17:44372763.
    Found 2 traits for chr17:42794089.
    Found 2 traits for chr17:42606002.
    Found 2 traits for chr17:41722828.
    Found 2 traits for chr17:40044663.
    Found 2 traits for chr17:40013510.
    Found 2 traits for chr17:39956807.
    Found 2 traits for chr17:3765193.
    Found 2 traits for chr17:35682510.
    Found 2 traits for chr17:29852702.
    Found 2 traits for chr17:29852694.
    Found 2 traits for chr17:29813599.
    Found 2 traits for chr17:2846566.
    Found 2 traits for chr17:2846560.
    Found 2 traits for chr17:2846555.
    Found 2 traits for chr17:2823905.
    Found 2 traits for chr17:26989948.
    Found 2 traits for chr17:22064831.
    Found 2 traits for chr17:21684971.
    Found 2 traits for chr17:18762161.
    Found 2 traits for chr17:1788527.
    Found 2 traits for chr17:17001857.
    Found 2 traits for chr17:16451843.
    Found 2 traits for chr17:16371521.
    Found 2 traits for chr17:1354830.
    Found 2 traits for chr17:1014620.
    Found 2 traits for chr16:89650787.
    Found 2 traits for chr16:85583635.
    Found 2 traits for chr16:81105873.
    Found 2 traits for chr16:754494.
    Found 2 traits for chr16:74615312.
    Found 2 traits for chr16:73895079.
    Found 2 traits for chr16:70218561.
    Found 2 traits for chr16:69576084.
    Found 2 traits for chr16:67610589.
    Found 2 traits for chr16:67096756.
    Found 2 traits for chr16:4771009.
    Found 2 traits for chr16:4590542.
    Found 2 traits for chr16:422947.
    Found 2 traits for chr16:3923860.
    Found 2 traits for chr16:36256874.
    Found 2 traits for chr16:34630340.
    Found 2 traits for chr16:34574822.
    Found 2 traits for chr16:3448877.
    Found 2 traits for chr16:34284927.
    Found 2 traits for chr16:34249700.
    Found 2 traits for chr16:32421947.
    Found 2 traits for chr16:30441816.
    Found 2 traits for chr16:30142456.
    Found 2 traits for chr16:2983500.
    Found 2 traits for chr16:29831442.
    Found 2 traits for chr16:2891984.
    Found 2 traits for chr16:2891980.
    Found 2 traits for chr16:2891973.
    Found 2 traits for chr16:28817467.
    Found 2 traits for chr16:28817462.
    Found 2 traits for chr16:24905612.
    Found 2 traits for chr16:23736647.
    Found 2 traits for chr16:23736646.
    Found 2 traits for chr16:21894065.
    Found 2 traits for chr16:21343238.
    Found 2 traits for chr16:20408737.
    Found 2 traits for chr16:18158393.
    Found 2 traits for chr16:15359814.
    Found 2 traits for chr16:1478801.
    Found 2 traits for chr16:11232146.
    Found 2 traits for chr15:92850975.
    Found 2 traits for chr15:90157092.
    Found 2 traits for chr15:90157090.
    Found 2 traits for chr15:90157089.
    Found 2 traits for chr15:78856334.
    Found 2 traits for chr15:77699496.
    Found 2 traits for chr15:74953400.
    Found 2 traits for chr15:65454700.
    Found 2 traits for chr15:65454698.
    Found 2 traits for chr15:63272088.
    Found 2 traits for chr15:55641955.
    Found 2 traits for chr15:55441294.
    Found 2 traits for chr15:41525481.
    Found 2 traits for chr15:41378616.
    Found 2 traits for chr15:41344055.
    Found 2 traits for chr15:40646627.
    Found 2 traits for chr15:34175843.
    Found 2 traits for chr15:28367284.
    Found 2 traits for chr15:23838964.
    Found 2 traits for chr15:23448633.
    Found 2 traits for chr15:23448605.
    Found 2 traits for chr15:23448602.
    Found 2 traits for chr14:92804368.
    Found 2 traits for chr14:92804364.
    Found 2 traits for chr14:92804358.
    Found 2 traits for chr14:91507096.
    Found 2 traits for chr14:91460178.
    Found 2 traits for chr14:91460172.
    Found 2 traits for chr14:73480223.
    Found 2 traits for chr14:72837563.
    Found 2 traits for chr14:71326357.
    Found 2 traits for chr14:71326352.
    Found 2 traits for chr14:66995017.
    Found 2 traits for chr14:65504716.
    Found 2 traits for chr14:64547356.
    Found 2 traits for chr14:63519957.
    Found 2 traits for chr14:39101400.
    Found 2 traits for chr14:34514025.
    Found 2 traits for chr14:28854140.
    Found 2 traits for chr14:28854137.
    Found 2 traits for chr14:28854133.
    Found 2 traits for chr14:22801564.
    Found 2 traits for chr14:20342296.
    Found 2 traits for chr14:105296067.
    Found 2 traits for chr14:103272252.
    Found 2 traits for chr14:102272953.
    Found 2 traits for chr14:102136332.
    Found 2 traits for chr13:99952132.
    Found 2 traits for chr13:49525439.
    Found 2 traits for chr13:49525435.
    Found 2 traits for chr13:21627637.
    Found 2 traits for chr13:20470690.
    Found 2 traits for chr13:111960813.
    Found 2 traits for chr13:110656083.
    Found 2 traits for chr12:94946026.
    Found 2 traits for chr12:7734982.
    Found 2 traits for chr12:762206.
    Found 2 traits for chr12:762205.
    Found 2 traits for chr12:73182069.
    Found 2 traits for chr12:64614426.
    Found 2 traits for chr12:64492451.
    Found 2 traits for chr12:57017048.
    Found 2 traits for chr12:56656759.
    Found 2 traits for chr12:56536657.
    Found 2 traits for chr12:54336354.
    Found 2 traits for chr12:53537240.
    Found 2 traits for chr12:53443104.
    Found 2 traits for chr12:53434594.
    Found 2 traits for chr12:51720075.
    Found 2 traits for chr12:50301840.
    Found 2 traits for chr12:50301831.
    Found 2 traits for chr12:50234243.
    Found 2 traits for chr12:50015583.
    Found 2 traits for chr12:50015024.
    Found 2 traits for chr12:48988805.
    Found 2 traits for chr12:42207674.
    Found 2 traits for chr12:41796537.
    Found 2 traits for chr12:37831033.
    Found 2 traits for chr12:31549775.
    Found 2 traits for chr12:31366840.
    Found 2 traits for chr12:26332904.
    Found 2 traits for chr12:18232057.
    Found 2 traits for chr12:132381811.
    Found 2 traits for chr12:130798250.
    Found 2 traits for chr12:12553080.
    Found 2 traits for chr12:125090208.
    Found 2 traits for chr12:124951817.
    Found 2 traits for chr12:124547226.
    Found 2 traits for chr12:123755925.
    Found 2 traits for chr12:123755449.
    Found 2 traits for chr12:121914347.
    Found 2 traits for chr12:121876914.
    Found 2 traits for chr12:121726344.
    Found 2 traits for chr12:121076600.
    Found 2 traits for chr12:120556370.
    Found 2 traits for chr12:112412911.
    Found 2 traits for chr12:101445846.
    Found 2 traits for chr11:77546029.
    Found 2 traits for chr11:73813823.
    Found 2 traits for chr11:67988509.
    Found 2 traits for chr11:67134652.
    Found 2 traits for chr11:65527549.
    Found 2 traits for chr11:65463200.
    Found 2 traits for chr11:64664247.
    Found 2 traits for chr11:63684993.
    Found 2 traits for chr11:62845315.
    Found 2 traits for chr11:62537111.
    Found 2 traits for chr11:62004372.
    Found 2 traits for chr11:55233796.
    Found 2 traits for chr11:54897486.
    Found 2 traits for chr11:47677751.
    Found 2 traits for chr11:47674901.
    Found 2 traits for chr11:46355253.
    Found 2 traits for chr11:3777426.
    Found 2 traits for chr11:33250072.
    Found 2 traits for chr11:31533623.
    Found 2 traits for chr11:30887986.
    Found 2 traits for chr11:30791372.
    Found 2 traits for chr11:2139063.
    Found 2 traits for chr11:12757946.
    Found 2 traits for chr11:117105931.
    Found 2 traits for chr11:1166174.
    Found 2 traits for chr11:113771108.
    Found 2 traits for chr11:1095580.
    Found 2 traits for chr10:84087114.
    Found 2 traits for chr10:73947669.
    Found 2 traits for chr10:70420363.
    Found 2 traits for chr10:5378108.
    Found 2 traits for chr10:41772584.
    Found 2 traits for chr10:41772582.
    Found 2 traits for chr10:17983189.
    Found 2 traits for chr10:14933466.
    Found 2 traits for chr10:14933460.
    Found 2 traits for chr10:132388816.
    Found 2 traits for chr10:128016654.
    Found 2 traits for chr10:12279639.
    Found 2 traits for chr10:12148087.
    Found 2 traits for chr10:119907251.
    Found 2 traits for chr10:119907249.
    Found 2 traits for chr10:119182166.
    Found 2 traits for chr10:111586970.
    Found 2 traits for chr10:111403547.
    Found 2 traits for chr10:102129838.
    Found 2 traits for chr1:90050171.
    Found 2 traits for chr1:88806458.
    Found 2 traits for chr1:8774498.
    Found 2 traits for chr1:63237607.
    Found 2 traits for chr1:5674860.
    Found 2 traits for chr1:54466274.
    Found 2 traits for chr1:54466273.
    Found 2 traits for chr1:52910512.
    Found 2 traits for chr1:52623572.
    Found 2 traits for chr1:46323003.
    Found 2 traits for chr1:46311457.
    Found 2 traits for chr1:43248985.
    Found 2 traits for chr1:42614311.
    Found 2 traits for chr1:40590746.
    Found 2 traits for chr1:40590743.
    Found 2 traits for chr1:39993465.
    Found 2 traits for chr1:39579706.
    Found 2 traits for chr1:39067815.
    Found 2 traits for chr1:3798817.
    Found 2 traits for chr1:37884766.
    Found 2 traits for chr1:37598789.
    Found 2 traits for chr1:35579988.
    Found 2 traits for chr1:35117555.
    Found 2 traits for chr1:28994406.
    Found 2 traits for chr1:28733121.
    Found 2 traits for chr1:28649645.
    Found 2 traits for chr1:28350749.
    Found 2 traits for chr1:26651472.
    Found 2 traits for chr1:26651469.
    Found 2 traits for chr1:26334710.
    Found 2 traits for chr1:26334705.
    Found 2 traits for chr1:25925787.
    Found 2 traits for chr1:241063951.
    Found 2 traits for chr1:226142102.
    Found 2 traits for chr1:22429164.
    Found 2 traits for chr1:224206818.
    Found 2 traits for chr1:22058633.
    Found 2 traits for chr1:220048851.
    Found 2 traits for chr1:220048846.
    Found 2 traits for chr1:214897967.
    Found 2 traits for chr1:20647193.
    Found 2 traits for chr1:203921617.
    Found 2 traits for chr1:200345291.
    Found 2 traits for chr1:200345290.
    Found 2 traits for chr1:200284721.
    Found 2 traits for chr1:200258342.
    Found 2 traits for chr1:197576732.
    Found 2 traits for chr1:179174275.
    Found 2 traits for chr1:17544542.
    Found 2 traits for chr1:17543895.
    Found 2 traits for chr1:17006395.
    Found 2 traits for chr1:16382341.
    Found 2 traits for chr1:161372350.
    Found 2 traits for chr1:161348819.
    Found 2 traits for chr1:15869308.
    Found 2 traits for chr1:155963921.
    Found 2 traits for chr1:155963917.
    Found 2 traits for chr1:155955307.
    Found 2 traits for chr1:155878634.
    Found 2 traits for chr1:155819546.
    Found 2 traits for chr1:155819544.
    Found 2 traits for chr1:152109644.
    Found 2 traits for chr1:152109643.
    Found 2 traits for chr1:151083907.
    Found 2 traits for chr1:150526800.
    Found 2 traits for chr1:150304119.
    Found 2 traits for chr1:149920006.
    Found 2 traits for chr1:148066859.
    Found 2 traits for chr1:148066853.
    Found 2 traits for chr1:148017587.
    Found 2 traits for chr1:148017581.
    Found 2 traits for chr1:144636407.
    Found 2 traits for chr1:143272167.
    Found 2 traits for chr1:143195687.
    Found 2 traits for chr1:1370032.
    Found 2 traits for chr1:1277184.
    Found 2 traits for chr1:10152050.
    Found 2 traits for rs905342119.
    Found 1 traits for rs77728772.
    Found 1 traits for rs77589046.
    Found 1 traits for rs72654463.
    Found 1 traits for rs72654461.
    Found 1 traits for rs6720234.
    Found 1 traits for rs6020921.
    Found 1 traits for rs573181360.
    Found 1 traits for rs56918975.
    Found 1 traits for rs56098445.
    Found 1 traits for rs557495347.
    Found 1 traits for rs553129131.
    Found 2 traits for rs551048812.
    Found 1 traits for rs541189631.
    Found 2 traits for rs537741299.
    Found 1 traits for rs4811115.
    Found 1 traits for rs4809823.
    Found 1 traits for rs446037.
    Found 1 traits for rs439382.
    Found 1 traits for rs435380.
    Found 1 traits for rs35114168.
    Found 1 traits for rs34278513.
    Found 1 traits for rs205909.
    Found 1 traits for rs200388554.
    Found 1 traits for rs186724723.
    Found 2 traits for rs183610051.
    Found 1 traits for rs182525847.
    Found 1 traits for rs166907.
    Found 1 traits for rs157598.
    Found 1 traits for rs157583.
    Found 1 traits for rs150214656.
    Found 2 traits for rs149661872.
    Found 1 traits for rs148998607.
    Found 1 traits for rs147440553.
    Found 1 traits for rs146444978.
    Found 1 traits for rs145654351.
    Found 1 traits for rs144618582.
    Found 1 traits for rs142978359.
    Found 1 traits for rs141744862.
    Found 1 traits for rs138229840.
    Found 1 traits for rs13009551.
    Found 1 traits for rs12480959.
    Found 1 traits for rs117806270.
    Found 1 traits for rs117323901.
    Found 1 traits for rs117010230.
    Found 1 traits for rs115908094.
    Found 2 traits for rs113492558.
    Found 2 traits for chr19:44905579.
    Found 2 traits for chr19:44901434.
    Found 1 traits for chr19:44895208.
    Found 1 traits for chr19:44882502.
    Found 1 traits for chr19:44864753.
    Found 1 traits for chr19:44799865.
    Found 1 traits for chr1:207633385.
    Found 1 traits for chr1:207630796.
    Found 1 traits for chr1:207629207.
    Found 1 traits for chr1:207627210.
    Found 1 traits for chr1:207626529.
    Found 1 traits for chr1:207625349.
    Found 1 traits for chr1:207623552.
    Found 1 traits for chr1:207613483.
    Found 1 traits for chr1:207613197.
    Found 1 traits for chr1:207612944.
    Found 1 traits for chr1:207611623.
    Found 1 traits for chr1:207573951.
    Found 1 traits for chr1:207512441.
    Found 1 traits for chr1:207510847.
    Found 1 traits for rs7529338.
    Found 1 traits for rs4505973.
    Found 1 traits for rs6961510.
    Found 1 traits for rs722069.
    Found 1 traits for rs9677610.
    Found 1 traits for rs74355737.
    Found 1 traits for rs35471107.
    Found 1 traits for rs142703147.
    Found 1 traits for rs117046683.
    Found 1 traits for rs113812546.
    Found 1 traits for rs28372356.
    Found 1 traits for rs16967952.
    Found 1 traits for rs748510.
    Found 1 traits for rs11920900.
    Found 1 traits for rs10802081.
    Found 1 traits for rs577934120.
    Found 1 traits for rs7530762.
    Found 1 traits for rs16350.
    Found 1 traits for rs530475031.
    Found 1 traits for rs138163369.
    Found 1 traits for rs558204720.
    Found 1 traits for rs140048432.
    Found 1 traits for rs10096834.
    Found 1 traits for rs527921556.
    Found 1 traits for rs557482905.
    Found 1 traits for rs183362544.
    Found 1 traits for rs12747038.
    Found 1 traits for rs28734019.
    Found 2 traits for rs116049755.
    Found 2 traits for rs17059069.
    Found 2 traits for rs12518991.
    Found 2 traits for rs2738752.
    Found 2 traits for rs476703.
    Found 2 traits for rs537924904.
    Found 2 traits for rs6533700.
    Found 2 traits for rs762021768.
    Found 2 traits for rs56353506.
    Found 2 traits for rs10514904.
    Found 2 traits for rs374891353.
    Found 2 traits for rs112003311.
    Found 2 traits for rs55896111.
    Found 2 traits for rs382362.
    Found 2 traits for rs35808754.
    Found 2 traits for rs11049512.
    Found 2 traits for rs11049587.
    Found 2 traits for rs7315807.
    Found 2 traits for rs557320816.
    Found 2 traits for rs74672221.
    Found 2 traits for rs147630278.
    Found 2 traits for rs74323244.
    Found 2 traits for rs373884000.
    Found 2 traits for rs12490052.
    Found 2 traits for rs3732444.
    Found 2 traits for rs578042620.
    Found 2 traits for rs112600995.
    Found 2 traits for rs1581335.
    Found 2 traits for rs309125.
    Found 2 traits for rs1372666.
    Found 2 traits for rs1335924.
    Found 2 traits for rs7554671.
    Found 2 traits for rs2154447.
    Found 2 traits for rs2425062.
    Found 2 traits for rs932552.
    Found 2 traits for rs7353271.
    Found 2 traits for rs752075.
    Found 2 traits for rs1502230.
    Found 2 traits for rs142805307.
    Found 2 traits for rs7505192.
    Found 2 traits for rs199440.
    Found 2 traits for rs1244020901.
    Found 2 traits for rs62074563.
    Found 2 traits for rs2684670.
    Found 2 traits for rs406400.
    Found 2 traits for rs4432221.
    Found 2 traits for rs1463570.
    Found 2 traits for rs10595541.
    Found 2 traits for rs7025117.
    Found 2 traits for rs10765987.
    Found 2 traits for rs371829168.
    Found 2 traits for rs263652.
    Found 2 traits for rs3135385.
    Found 2 traits for rs9267101.
    Found 2 traits for rs3130437.
    Found 2 traits for rs886400.
    Found 2 traits for rs2517552.
    Found 2 traits for rs6932954.
    Found 2 traits for rs1482679.
    Found 2 traits for rs2034347.
    Found 2 traits for rs11730365.
    Found 2 traits for rs13071551.
    Found 2 traits for rs10556352.
    Found 2 traits for rs77767153.
    Found 2 traits for rs10746406.
    Found 2 traits for rs11118662.
    Found 2 traits for rs12139563.
    Found 2 traits for rs11222875.
    Found 2 traits for rs113498532.
    Found 2 traits for rs12493744.
    Found 2 traits for rs2451714.
    Found 2 traits for rs28780068.
    Found 2 traits for rs3794703.
    Found 2 traits for rs9267287.
    Found 2 traits for rs17178377.
    Found 2 traits for rs5840139.
    Found 2 traits for rs145995246.
    Found 2 traits for rs539064712.
    Found 2 traits for rs10221243.
    Found 2 traits for rs1191835359.
    Found 2 traits for rs111748739.
    Found 2 traits for rs67656733.
    Found 2 traits for rs10451283.
    Found 2 traits for rs112164745.
    Found 2 traits for rs2903705.
    Found 2 traits for rs11645016.
    Found 2 traits for rs3751847.
    Found 2 traits for rs2285221.
    Found 2 traits for rs2869031.
    Found 2 traits for rs148876555.
    Found 2 traits for rs12317339.
    Found 2 traits for rs58720921.
    Found 2 traits for rs7964793.
    Found 2 traits for rs1797493.
    Found 2 traits for rs4980006.
    Found 2 traits for rs60033539.
    Found 2 traits for rs535700953.
    Found 2 traits for rs7776318.
    Found 2 traits for rs200864308.
    Found 2 traits for rs204989.
    Found 2 traits for rs3134780.
    Found 2 traits for rs9257703.
    Found 2 traits for rs17336532.
    Found 2 traits for rs74221529.
    Found 2 traits for rs3799499.
    Found 2 traits for rs11724589.
    Found 2 traits for rs60611116.
    Found 2 traits for rs34883520.
    Found 2 traits for rs112819759.
    Found 2 traits for rs76552657.
    Found 2 traits for rs6808391.
    Found 2 traits for rs73147943.
    Found 2 traits for rs141948832.
    Found 2 traits for rs7634335.
    Found 2 traits for rs62126405.
    Found 2 traits for rs151296623.
    Found 2 traits for rs55865752.
    Found 2 traits for rs3049985.
    Found 2 traits for rs750559.
    Found 2 traits for rs6060302.
    Found 2 traits for rs12602509.
    Found 2 traits for rs72869461.
    Found 2 traits for rs17759236.
    Found 2 traits for rs2693333.
    Found 2 traits for rs71833723.
    Found 2 traits for rs572171337.
    Found 2 traits for rs1706722.
    Found 2 traits for rs200133133.
    Found 2 traits for rs56226101.
    Found 2 traits for rs2381031.
    Found 2 traits for rs58839531.
    Found 2 traits for rs480629.
    Found 2 traits for rs33092.
    Found 2 traits for rs34125625.
    Found 2 traits for rs846598.
    Found 2 traits for rs2395427.
    Found 2 traits for rs10824429.
    Found 2 traits for rs2579723.
    Found 2 traits for rs1090026.
    Found 2 traits for rs10117493.
    Found 2 traits for rs6989059.
    Found 2 traits for rs10260131.
    Found 2 traits for rs646414.
    Found 2 traits for rs611802.
    Found 2 traits for rs113628279.
    Found 2 traits for rs9496302.
    Found 2 traits for rs572019305.
    Found 2 traits for rs7763411.
    Found 2 traits for rs9265949.
    Found 2 traits for rs1059612.
    Found 2 traits for rs3094064.
    Found 2 traits for rs1150732.
    Found 2 traits for rs71548317.
    Found 2 traits for rs76091509.
    Found 2 traits for rs55745041.
    Found 2 traits for rs11438876.
    Found 2 traits for rs148535531.
    Found 2 traits for rs17036076.
    Found 2 traits for rs13058813.
    Found 2 traits for rs34797591.
    Found 2 traits for rs922526.
    Found 2 traits for rs13081172.
    Found 2 traits for rs200099684.
    Found 2 traits for rs9864997.
    Found 2 traits for rs6762231.
    Found 2 traits for rs774732.
    Found 2 traits for rs13100122.
    Found 2 traits for rs34393323.
    Found 2 traits for rs79277477.
    Found 2 traits for rs116543468.
    Found 1 traits for rs3000062.
    Found 1 traits for rs79943871.
    Found 1 traits for rs74609633.
    Found 1 traits for chr11:4583187.
    Found 1 traits for chr3:104268937.
    Found 1 traits for chr3:11673254.
    Found 1 traits for rs442999.
    Found 1 traits for chr18:67733107.
    Found 1 traits for rs374474040.
    Found 1 traits for rs76843548.
    Found 1 traits for rs111991386.
    Found 1 traits for rs142982897.
    Found 1 traits for rs1808021.
    Found 1 traits for rs10940985.
    Found 1 traits for rs6973776.
    Found 1 traits for rs138414619.
    Found 1 traits for rs3111647.
    Found 1 traits for rs3733190.
    Found 1 traits for rs10082433.
    Found 1 traits for rs76308053.
    Found 1 traits for rs28867695.
    Found 1 traits for rs75611237.
    Found 1 traits for rs72938403.
    Found 1 traits for rs542976303.
    Found 1 traits for rs1278808601.
    Found 1 traits for rs113033440.
    Found 1 traits for chr2:208171466.
    Found 1 traits for rs7206464.
    Found 1 traits for rs12676238.
    Found 1 traits for rs78370368.
    Found 1 traits for chr4:60663814.
    Found 1 traits for rs62013778.
    Found 1 traits for rs117656276.
    Found 1 traits for rs11637528.
    Found 1 traits for rs117583829.
    Found 1 traits for rs10810586.
    Found 1 traits for chr9:15809816.
    Found 1 traits for chr2:41081216.
    Found 1 traits for chr3:38553482.
    Found 1 traits for chr7:21912139.
    Found 1 traits for rs13108726.
    Found 1 traits for chr9:107111712.
    Found 1 traits for chr1:164000575.
    Found 1 traits for rs184369354.
    Found 1 traits for rs141134162.
    Found 1 traits for rs79692770.
    Found 1 traits for rs6834396.
    Found 1 traits for rs149750027.
    Found 1 traits for rs878870427.
    Found 1 traits for rs74361068.
    Found 1 traits for rs4938599.
    Found 1 traits for rs55933931.
    Found 1 traits for rs139840189.
    Found 1 traits for rs2459703.
    Found 1 traits for rs1592655.
    Found 1 traits for chr11:70580250.
    Found 1 traits for rs144109402.
    Found 1 traits for chr11:2130822.
    Found 1 traits for rs72671358.
    Found 1 traits for chr2:71161890.
    Found 1 traits for rs191909134.
    Found 1 traits for rs183285716.
    Found 1 traits for rs10906573.
    Found 1 traits for rs73106066.
    Found 1 traits for rs28711799.
    Found 1 traits for chr12:107687840.
    Found 1 traits for rs141805262.
    Found 1 traits for chr6:163063957.
    Found 1 traits for rs188498538.
    Found 1 traits for rs4968184.
    Found 1 traits for chr12:105673516.
    Found 1 traits for rs117518840.
    Found 1 traits for chr4:21453773.
    Found 1 traits for rs12672895.
    Found 1 traits for rs79978310.
    Found 1 traits for rs144664881.
    Found 1 traits for rs690355.
    Found 1 traits for rs56119286.
    Found 1 traits for rs3760709.
    Found 1 traits for chr11:31849627.
    Found 1 traits for rs75846478.
    Found 1 traits for rs10142874.
    Found 1 traits for rs9309695.
    Found 1 traits for rs80335568.
    Found 1 traits for rs3810292.
    Found 1 traits for rs140877290.
    Found 1 traits for rs2930147.
    Found 1 traits for rs140539354.
    Found 1 traits for chr3:166547253.
    Found 1 traits for rs8077401.
    Found 1 traits for rs1036686.
    Found 1 traits for rs2618721.
    Found 1 traits for chr15:92034733.
    Found 1 traits for rs3814811.
    Found 1 traits for rs2496034.
    Found 1 traits for rs62394321.
    Found 1 traits for rs12570655.
    Found 1 traits for rs187101883.
    Found 1 traits for chr8:61771734.
    Found 1 traits for rs76220751.
    Found 1 traits for chr8:124757713.
    Found 1 traits for rs74928517.
    Found 1 traits for rs66517027.
    Found 1 traits for rs61954170.
    Found 1 traits for rs13104559.
    Found 1 traits for rs73568808.
    Found 1 traits for chr4:22377427.
    Found 1 traits for rs6496412.
    Found 1 traits for rs117667073.
    Found 1 traits for rs10148392.
    Found 1 traits for rs10970557.
    Found 1 traits for rs2344570.
    Found 1 traits for chr11:95508376.
    Found 1 traits for rs73383397.
    Found 1 traits for chr10:3077456.
    Found 1 traits for rs7038286.
    Found 1 traits for rs6856358.
    Found 1 traits for rs8014370.
    Found 1 traits for rs147292770.
    Found 1 traits for rs7858563.
    Found 1 traits for rs463240.
    Found 1 traits for rs16873274.
    Found 1 traits for rs191469196.
    Found 1 traits for chr3:45780132.
    Found 1 traits for rs182748462.
    Found 1 traits for rs7859269.
    Found 1 traits for rs117421904.
    Found 1 traits for rs12783688.
    Found 1 traits for rs371353403.
    Found 1 traits for rs34988354.
    Found 1 traits for rs12110226.
    Found 1 traits for chr15:50175552.
    Found 1 traits for rs630123.
    Found 1 traits for rs11860760.
    Found 1 traits for chr13:29357481.
    Found 1 traits for rs71414122.
    Found 1 traits for rs111394117.
    Found 1 traits for chr9:83321403.
    Found 1 traits for chr9:75351774.
    Found 1 traits for chr8:62917168.
    Found 1 traits for rs11087006.
    Found 1 traits for chr4:177829610.
    Found 1 traits for chr3:2822536.
    Found 1 traits for rs117380768.
    Found 1 traits for rs13247786.
    Found 1 traits for chr5:563190.
    Found 1 traits for chr5:180195618.
    Found 1 traits for rs9560035.
    Found 1 traits for rs73220062.
    Found 1 traits for rs12439381.
    Found 1 traits for rs2307326.
    Found 1 traits for rs76890566.
    Found 1 traits for rs1958144.
    Found 1 traits for rs3783076.
    Found 1 traits for rs1485983039.
    Found 1 traits for rs17144624.
    Found 1 traits for chr12:20445043.
    Found 1 traits for rs2784953.
    Found 1 traits for rs149580969.
    Found 1 traits for rs11744003.
    Found 1 traits for rs3804994.
    Found 1 traits for rs192388340.
    Found 1 traits for rs867484906.
    Found 1 traits for rs13175350.
    Found 1 traits for chr7:4521834.
    Found 1 traits for rs79868062.
    Found 1 traits for rs112844157.
    Found 1 traits for rs140858987.
    Found 1 traits for chr11:119608963.
    
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
      <td>rs117741236</td>
      <td>depressive disorder</td>
    </tr>
    <tr>
      <th>1</th>
      <td>rs1853229</td>
      <td>depressive disorder</td>
    </tr>
    <tr>
      <th>2</th>
      <td>rs138801403</td>
      <td>depressive disorder</td>
    </tr>
    <tr>
      <th>3</th>
      <td>rs56289435</td>
      <td>depressive disorder</td>
    </tr>
    <tr>
      <th>4</th>
      <td>rs4786119</td>
      <td>depressive disorder</td>
    </tr>
    <tr>
      <th>...</th>
      <td>...</td>
      <td>...</td>
    </tr>
    <tr>
      <th>4177</th>
      <td>chr7:4521834</td>
      <td>X-15503 measurement</td>
    </tr>
    <tr>
      <th>4178</th>
      <td>rs79868062</td>
      <td>X-15503 measurement</td>
    </tr>
    <tr>
      <th>4179</th>
      <td>rs112844157</td>
      <td>X-15503 measurement</td>
    </tr>
    <tr>
      <th>4180</th>
      <td>rs140858987</td>
      <td>X-13728 measurement</td>
    </tr>
    <tr>
      <th>4181</th>
      <td>chr11:119608963</td>
      <td>X-13728 measurement</td>
    </tr>
  </tbody>
</table>
<p>4182 rows × 2 columns</p>
</div>


### Question 12: Which variants are associated with an environmental factor on disease outcome? Eg: variants associated with smoking on lung cancer (GxE)



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
        print(f"Page {current_page + 1}/{total_pages} processed. Found {len(gxe_study_accessions)} GxE studies.")
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
        print(f"Checked study {accession_id}, found {len(gxe_associations)} matching associations so far.")

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
    Page 1/1 processed. Found 3 GxE studies.
    
    Found 3 GxE studies for 'total blood protein measurement'.
    
    ===== Step 2: Searching for 'diet' factor within these studies =====
    --- Starting search for all associations related to 'GCST90161226' ---
    Found 10 total associations across 1 pages.
    Page 1/1 processed. Collected 10 associations so far.
    --- Finished fetching. Found 10 total associations. ---
    
    Checked study GCST90161226, found 10 matching associations so far.
    --- Starting search for all associations related to 'GCST90161196' ---
    Found 10 total associations across 1 pages.
    Page 1/1 processed. Collected 10 associations so far.
    --- Finished fetching. Found 10 total associations. ---
    
    Checked study GCST90161196, found 20 matching associations so far.
    --- Starting search for all associations related to 'GCST90026658' ---
    Found 266 total associations across 2 pages.
    Page 1/2 processed. Collected 200 associations so far.
    Page 2/2 processed. Collected 266 associations so far.
    --- Finished fetching. Found 266 total associations. ---
    
    Checked study GCST90026658, found 20 matching associations so far.
    
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


### Question 13: Which genomic region has the most variants associated with a disease? eg: the genomic region with most variants for type 2 diabetes


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
        if 'snp_risk_allele' in association and association['snp_risk_allele']:
            rs_id = association['snp_risk_allele'][0].split('-')[0]
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
    Error: Received status code 500
    URL: https://wwwdev.ebi.ac.uk/gwas/beta/rest/api/v2/associations?efo_trait=type+2+diabetes+mellitus&sort=p_value&direction=asc&size=200&page=0
    Response: {"timestamp":"2025-07-25T15:55:35.620+00:00","status":500,"error":"Internal Server Error","path":"/gwas/beta/rest/api/v2/associations"}
    No more data or an error occurred. Stopping.
    --- Finished fetching. Found 0 total associations. ---
    
    Found 0 unique variants from the top associations.
    
    --- Analysis Complete ---
    Could not retrieve region information for the variants.


### Question 14. Which SNP has the strongest effect size/OR for a disease? eg: The SNP with the strongest effect size/OR for type 2 diabetes


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
    risk_allele_str = results_df['snp_risk_allele'].str[0]
    split_allele = risk_allele_str.str.split('-', n=1, expand=True)
    results_df['variant_rsID'] = split_allele[0]
    results_df['risk_allele_base'] = split_allele[1]

    # Set display format for floats
    pd.options.display.float_format = '{:.5f}'.format

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
      <td>0.00000</td>
      <td>rs77989445</td>
      <td>0.1436</td>
      <td>GCST006484</td>
    </tr>
  </tbody>
</table>
</div>


### Question 15. How many studies report a specific gene associated with a specific disease? eg: number of the studies reporting the KCNQ1 gene associated with type 2 diabetes


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
    Error: Received status code 500
    URL: https://wwwdev.ebi.ac.uk/gwas/beta/rest/api/v2/associations?efo_trait=type+2+diabetes+mellitus&sort=p_value&direction=asc&size=200&page=0
    Response: {"timestamp":"2025-07-25T15:55:37.932+00:00","status":500,"error":"Internal Server Error","path":"/gwas/beta/rest/api/v2/associations"}
    No more data or an error occurred. Stopping.
    --- Finished fetching. Found 0 total associations. ---
    
    
    --- Search Complete ---
    Could not find any studies reporting 'KCNQ1' for 'type 2 diabetes mellitus'.


### Question 16. What are the sample sizes used for the top 10 significant variants for disease, eg: find the top 10 significant variants associated with type 2 diabetes and find their sample size



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
    risk_allele_str = results_df['snp_risk_allele'].str[0]
    split_allele = risk_allele_str.str.split('-', n=1, expand=True)
    results_df['variant_rsID'] = split_allele[0]
    
    # Set pandas display format for p-values
    pd.options.display.float_format = '{:.20f}'.format
    
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
      <td>0.00000000000000000000</td>
      <td>rs7903146</td>
      <td>50,251 African American cases, 103,909 African...</td>
      <td>GCST90492734</td>
    </tr>
    <tr>
      <th>1</th>
      <td>0.00000000000000000000</td>
      <td>rs7903146</td>
      <td>148,726 European ancestry cases, 965,732 Europ...</td>
      <td>GCST010555</td>
    </tr>
    <tr>
      <th>2</th>
      <td>0.00000000000000000000</td>
      <td>rs35011184</td>
      <td>148,726 European ancestry cases, 24,646 Africa...</td>
      <td>GCST010557</td>
    </tr>
    <tr>
      <th>3</th>
      <td>0.00000000000000000000</td>
      <td>rs7903146</td>
      <td>51,256 African, African American, East Asian, ...</td>
      <td>GCST90444202</td>
    </tr>
    <tr>
      <th>4</th>
      <td>0.00000000000000000000</td>
      <td>rs2237897</td>
      <td>50,251 African American cases, 103,909 African...</td>
      <td>GCST90492734</td>
    </tr>
    <tr>
      <th>5</th>
      <td>0.00000000000000000000</td>
      <td>rs7903146</td>
      <td>251,740 European ancestry individuals, 139,705...</td>
      <td>GCST90132183</td>
    </tr>
    <tr>
      <th>6</th>
      <td>0.00000000000000000000</td>
      <td>rs7903146</td>
      <td>74,124 European ancestry cases, 824,006 Europe...</td>
      <td>GCST009379</td>
    </tr>
    <tr>
      <th>7</th>
      <td>0.00000000000000000000</td>
      <td>rs7766070</td>
      <td>50,251 African American cases, 103,909 African...</td>
      <td>GCST90492734</td>
    </tr>
    <tr>
      <th>8</th>
      <td>0.00000000000000000000</td>
      <td>rs10811661</td>
      <td>50,251 African American cases, 103,909 African...</td>
      <td>GCST90492734</td>
    </tr>
    <tr>
      <th>9</th>
      <td>0.00000000000000000000</td>
      <td>rs7903146</td>
      <td>61,714 European ancestry cases, 1,178 Pakistan...</td>
      <td>GCST006867</td>
    </tr>
  </tbody>
</table>
</div>



```python

```
